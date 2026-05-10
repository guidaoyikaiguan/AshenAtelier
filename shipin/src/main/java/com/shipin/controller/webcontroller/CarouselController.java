package com.shipin.controller.webcontroller;

import com.qiniu.util.Auth;
import com.shipin.entity.po.Carousel;
import com.shipin.entity.query.CarouselQuery;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;
import com.shipin.service.CarouselService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("carouselController")
@RequestMapping("/api/carousel")
public class CarouselController {
    @Resource
    private CarouselService carouselService;

    @Value("${qiniu.access-key}")
    private String accessKey;
    @Value("${qiniu.secret-key}")
    private String secretKey;
    @Value("${qiniu.bucket-name}")
    private String bucketName;
    @Value("${qiniu.domain}")
    private String domain;
    //主页获取全部的轮播图，限制为5条，按照sort_order排序
    @RequestMapping("/getCarousel")
    public Result getCarousel() {
        CarouselQuery query = new CarouselQuery();
        query.setStatus(1); // 只获取启用的轮播图
        query.setOrderBy("sort_order asc");
        query.setPageSize(5);
        PaginationResultVO<Carousel> listByPage = carouselService.findListByPage(query);
        for(Carousel carousel : listByPage.getList()) {
            String cover = carousel.getCover();
            if (cover != null && cover.contains(domain)) {
                String key = cover.substring(cover.lastIndexOf("/") + 1);
                String signedUrl = getPrivateUrl(key);
                carousel.setCover(signedUrl);
            }
        }
        return Result.success(listByPage.getList());
    }
    //按照不同的分类获取轮播图
    @RequestMapping("/getCarouselByCategory")
    public Result getCarouselByCategory(Integer categoryId) {
        CarouselQuery query = new CarouselQuery();
        query.setStatus(1); // 只获取启用的轮播图
        query.setCategoryId(categoryId);
        query.setOrderBy("sort_order asc");
        query.setPageSize(5);
        PaginationResultVO<Carousel> listByPage = carouselService.findListByPage(query);
        for(Carousel carousel : listByPage.getList()) {
            String cover = carousel.getCover();
            if (cover != null && cover.contains(domain)) {
                String key = cover.substring(cover.lastIndexOf("/") + 1);
                String signedUrl = getPrivateUrl(key);
                carousel.setCover(signedUrl);
            }
        }
        return Result.success(listByPage.getList());
    }
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
}
