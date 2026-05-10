package com.shipin.controller.admincontroller;

import com.qiniu.util.Auth;
import com.shipin.annotation.AdminRequired;
import com.shipin.entity.po.Carousel;
import com.shipin.entity.po.Category;
import com.shipin.entity.query.CarouselQuery;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;
import com.shipin.service.CarouselService;
import com.shipin.service.CategoryService;
import com.shipin.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController("carouselAdminController")
@RequestMapping("/api/admin/carousel")
public class CarouselAdminController {
    @Resource
    private CarouselService carouselService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private JwtUtil jwtUtil;
    @Value("${qiniu.access-key}")
    private String accessKey;
    @Value("${qiniu.secret-key}")
    private String secretKey;
    @Value("${qiniu.bucket-name}")
    private String bucketName;
    @Value("${qiniu.domain}")
    private String domain;

    //管理端获取全部的轮播图列表
    @AdminRequired
    @RequestMapping("/list")
    public Result getCarouselList(Integer pageNo) {
        CarouselQuery carouselQuery = new CarouselQuery();
        carouselQuery.setPageNo(pageNo);
        carouselQuery.setPageSize(10);
        PaginationResultVO<Carousel> listByPage = carouselService.findListByPage(carouselQuery);
        for (Carousel carousel : listByPage.getList()) {
            Category categoryByCategoryId = categoryService.getCategoryByCategoryId(carousel.getCategoryId());
            if (categoryByCategoryId != null) {
                carousel.setCategoryName(categoryByCategoryId.getCategoryName());
            }
            // 处理私有空间的图片URL（添加访问签名）
            String cover = carousel.getCover();
            if (cover != null && cover.contains(domain)) {
                String key = cover.substring(cover.lastIndexOf("/") + 1);
                String signedUrl = getPrivateUrl(key);
                carousel.setCover(signedUrl);
            }
        }
        return Result.success(listByPage);
    }
    //根据分类查询轮播图列表
    @RequestMapping("/getCarouselByCategoryId")
    @AdminRequired
    public Result getCarouselByCategoryId(Integer pageNo,Integer categoryId) {
        CarouselQuery carouselQuery = new CarouselQuery();
        carouselQuery.setCategoryId(categoryId);
        carouselQuery.setPageNo(pageNo);
        carouselQuery.setPageSize(10);
        PaginationResultVO<Carousel> listByPage = carouselService.findListByPage(carouselQuery);
        for (Carousel carousel : listByPage.getList()) {
            Category categoryByCategoryId = categoryService.getCategoryByCategoryId(carousel.getCategoryId());
            if (categoryByCategoryId != null) {
                carousel.setCategoryName(categoryByCategoryId.getCategoryName());
            }
            // 处理私有空间的图片URL（添加访问签名）
            String cover = carousel.getCover();
            if (cover != null && cover.contains(domain)) {
                String key = cover.substring(cover.lastIndexOf("/") + 1);
                String signedUrl = getPrivateUrl(key);
                carousel.setCover(signedUrl);
            }
        }
        return Result.success(listByPage);
    }
    @GetMapping("/getQiniuToken")
    public Result getQiniuToken(HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // 生成上传令牌
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucketName);

            System.out.println("生成的七牛云上传令牌: " + upToken);
            System.out.println("使用的存储桶名称: " + bucketName);

            return Result.success(upToken);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取上传令牌失败");
        }
    }

    // 生成带签名的图片URL（用于私有存储桶）
    @GetMapping("/getSignedUrl")
    public Result getSignedUrl(@RequestParam String key, HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // 生成带签名的URL，有效期3600秒
            String signedUrl = getPrivateUrl(key);

            System.out.println("生成的带签名URL: " + signedUrl);

            return Result.success(signedUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "生成签名URL失败");
        }
    }
    // 新增轮播图
    @AdminRequired
    @RequestMapping("/add")
    public Result addCarousel(@RequestBody Carousel carousel) {
        try {
            // 验证参数
            System.out.println(carousel.getTitle());
            if (carousel.getCategoryId() == null) {
                return Result.error("分类ID不能为空");
            }
            if (carousel.getTitle() == null || carousel.getTitle().trim().isEmpty()) {
                return Result.error("轮播图标题不能为空");
            }
            if (carousel.getCover() == null || carousel.getCover().trim().isEmpty()) {
                return Result.error("轮播图图片不能为空");
            }

            // 设置默认值
            if (carousel.getStatus() == null) {
                carousel.setStatus(1); // 默认启用
            }
            if (carousel.getSortOrder() == null) {
                carousel.setSortOrder(0); // 默认排序
            }
            carousel.setCreateTime(new java.util.Date());
            carousel.setUpdateTime(new java.util.Date());

            // 保存轮播图
            Integer count = carouselService.add(carousel);
            if (count > 0) {
                return Result.success("添加轮播图成功");
            } else {
                return Result.error("添加轮播图失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加轮播图失败");
        }
    }
    //更新轮播图
    @AdminRequired
    @RequestMapping("/update")
    public Result updateCarousel(@RequestBody Carousel carousel) {
        try {
            // 验证参数
            if(carousel.getCarouselId() == null) {
                return Result.error("轮播图ID不能为空");
            }
            if (carousel.getCategoryId() == null) {
                return Result.error("分类ID不能为空");
            }
            if (carousel.getTitle() == null || carousel.getTitle().trim().isEmpty()) {
                return Result.error("轮播图标题不能为空");
            }
            if (carousel.getCover() == null || carousel.getCover().trim().isEmpty()) {
                return Result.error("轮播图图片不能为空");
            }
            carousel.setUpdateTime(new java.util.Date());
            // 更新轮播图
            Integer count=carouselService.updateCarouselByCarouselId(carousel, carousel.getCarouselId());
            if(count>0){
                return Result.success("更新轮播图成功");
            }else {
                return Result.error("更新轮播图失败");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新轮播图失败");
        }
    }
    //删除轮播图
    @AdminRequired
    @RequestMapping("/delete")
    public Result deleteCarousel(Integer carouselId) {
        try {
            if(carouselId == null) {
                return Result.error("轮播图ID不能为空");
            }
            CarouselQuery carouselQuery = new CarouselQuery();
            carouselQuery.setCarouselId(carouselId);
            Integer count=carouselService.deleteByParam(carouselQuery);
            if(count>0){
                return Result.success("删除轮播图成功");
            }else {
                return Result.error("删除轮播图失败");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除轮播图失败");
        }
    }
    //批量删除轮播图
    @AdminRequired
    @RequestMapping("/deleteBatch")
    public Result deleteBatchCarousel(Integer[] carouselIds) {
        try {
            if(carouselIds == null || carouselIds.length == 0) {
                return Result.error("轮播图ID不能为空");
            }
            for(Integer carouselId : carouselIds) {
                CarouselQuery carouselQuery = new CarouselQuery();
                carouselQuery.setCarouselId(carouselId);
                Integer count=carouselService.deleteByParam(carouselQuery);
                if(count==0){
                    return Result.error("删除轮播图失败");
                }
            }
            return Result.success("批量删除轮播图成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除轮播图失败");
        }
    }
    // 切换轮播图状态
    @AdminRequired
    @RequestMapping("/toggleStatus")
    public Result toggleStatus(Integer carouselId, Integer status, HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // 验证参数
            if (carouselId == null) {
                return Result.error("轮播图ID不能为空");
            }
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态值不正确");
            }

            // 更新状态
            Carousel carousel = new Carousel();
            carousel.setStatus(status);
            carousel.setUpdateTime(new java.util.Date());
            Integer count = carouselService.updateCarouselByCarouselId(carousel, carouselId);
            if (count > 0) {
                return Result.success("切换状态成功");
            } else {
                return Result.error("切换状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("切换状态失败");
        }
    }

    /**
     * 获取七牛云私有空间的访问签名URL
     */
    private String getPrivateUrl(String key) {
        Auth auth = Auth.create(accessKey, secretKey);
        String baseUrl = "http://" + domain + "/" + key;
        long expireInSeconds = 3600; // 1小时过期
        String signedUrl = auth.privateDownloadUrl(baseUrl, expireInSeconds);
        // 确保返回的URL包含http://前缀
        if (!signedUrl.startsWith("http://") && !signedUrl.startsWith("https://")) {
            signedUrl = "http://" + signedUrl;
        }
        System.out.println("生成的签名URL: " + signedUrl);
        return signedUrl;
    }
    //获取共有多少轮播图
    @AdminRequired
    @RequestMapping("/countCarousel")
    public Result countCarousel() {
        try {
            CarouselQuery carouselQuery = new CarouselQuery();
            Integer count = carouselService.findCountByParam(carouselQuery);
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取轮播图总数失败");
        }
    }
    //获取当前分类有多少轮播图
    @AdminRequired
    @RequestMapping("/countCarouselByCategoryId")
    public Result countCarouselByCategoryId(Integer categoryId) {
        try {
            if (categoryId == null) {
                return Result.error("分类ID不能为空");
            }
            CarouselQuery carouselQuery = new CarouselQuery();
            carouselQuery.setCategoryId(categoryId);
            Integer count = carouselService.findCountByParam(carouselQuery);
            return Result.success(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("按照分类获取轮播图总数失败");
        }
    }
}
