package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.Collect;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.CollectQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;
import com.shipin.service.CollectService;
import com.shipin.service.VideoService;
import com.shipin.utils.JwtUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController("collectController")
@RequestMapping("/api/collect")
public class CollectController {
    @Resource
    private CollectService collectService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private VideoService videoService;

    //用户查询自己的收藏
    @RequestMapping("/getCollectList")
    @LoginRequired
    public Result<List<Video>> getCollectList(HttpServletRequest request, Integer favoriteNO, Integer pageNo, Integer pageSize,Integer sortord) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userIdFromToken);
        collectQuery.setFavorite(favoriteNO);
        collectQuery.setPageNo(pageNo);
        collectQuery.setPageSize(pageSize);
        List<Video> videoList = new ArrayList();
        List<Collect> collectList=new ArrayList<>();
        if(sortord == 1){
            collectList=collectService.selectOrderByTimeAsc(collectQuery).getList();
        }
        if(sortord == 2){
            collectList=collectService.selectOrderByPlayCount(collectQuery).getList();
        }
        if(sortord == 3){
            collectList=collectService.selectOrderByVideoTime(collectQuery).getList();
        }
        for(Collect collect : collectList){
            Integer videoId = collect.getVideoId();
            // 只有当videoId不为NULL时，才尝试获取视频信息
            if(videoId != null){
                VideoQuery videoQuery = new VideoQuery();
                videoQuery.setVideoId(videoId);
                List<Video> videos = videoService.selectVideo(videoQuery);
                if(videos != null && !videos.isEmpty()){
                    videoList.add(videos.get(0));
                }
            }
        }
        return Result.success(videoList);
    }
    //获取当前收藏夹的有多少视频
    @LoginRequired
    @RequestMapping("/getCollectCount")
    public Result<Integer> getCollectCount(HttpServletRequest request, Integer favoriteNO) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userIdFromToken);
        collectQuery.setFavorite(favoriteNO);
        // 获取收藏夹的所有记录
        List<Collect> collectList = collectService.findListByParam(collectQuery);
        // 只计算videoId不为NULL的记录
        int validCount = 0;
        for(Collect collect : collectList){
            if(collect.getVideoId() != null){
                validCount++;
            }
        }
        return Result.success(validCount);
    }
    //获取左侧收藏夹导航栏
    @LoginRequired
    @RequestMapping("/getFavorite")
    public Result<List<Map<String, Object>>> getFavorite(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userIdFromToken);
        List<Collect> collectList = collectService.findListByParam(collectQuery);
        
        // 按favorite_name分组，计算每个收藏夹的视频数量
        Map<String, Collect> favoriteMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        
        for(Collect collect : collectList){
            String favoriteName = collect.getFavoriteName();
            if(favoriteName != null && !favoriteName.isEmpty()){
                // 如果是新的收藏夹，添加到map中
                if(!favoriteMap.containsKey(favoriteName)){
                    favoriteMap.put(favoriteName, collect);
                    countMap.put(favoriteName, 0);
                }
                // 如果videoId不为NULL，增加计数
                if(collect.getVideoId() != null){
                    countMap.put(favoriteName, countMap.get(favoriteName) + 1);
                }
            }
        }
        
        // 转换为列表并设置计数
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(Map.Entry<String, Collect> entry : favoriteMap.entrySet()){
            String favoriteName = entry.getKey();
            Collect collect = entry.getValue();
            Integer count = countMap.get(favoriteName);
            
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteName", favoriteName);
            item.put("favorite", collect.getFavorite());
            item.put("count", count);
            item.put("isPublic", collect.getIsPublic());
            
            resultList.add(item);
        }
        
        return Result.success(resultList);
    }

    // 修改收藏夹状态
    @LoginRequired
    @RequestMapping("/updateCollectStatus")
    public Result <Void> updateCollectStatus(HttpServletRequest request, Integer favorite, Collect collect){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setFavorite(favorite);
        collectQuery.setUserId(userIdFromToken);

        // 检查收藏夹是否存在
        List<Collect> collectList = collectService.findListByParam(collectQuery);
        if(collectList == null || collectList.isEmpty()){
            return Result.error("收藏夹不存在或不属于该用户");
        }

        // 构建更新对象
        Collect updateCollect = new Collect();
        updateCollect.setFavoriteName(collect.getFavoriteName());
        updateCollect.setIsPublic(collect.getIsPublic());

        // 执行更新
        collectService.updateByParam(updateCollect, collectQuery);
        return Result.success();
    }
    //新增收藏夹
    @LoginRequired
    @RequestMapping("/addCollect")
    public Result<Void> addCollect(HttpServletRequest request, Collect collect){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);

        // 参数校验
        if (collect.getFavoriteName() == null || collect.getFavoriteName().trim().isEmpty()) {
            return Result.error("收藏夹名称不能为空");
        }

        // 获取当前用户最新的收藏夹编号
        Integer newFavoriteNo = collectService.selectFavoriteNo(userIdFromToken);
        collect.setFavorite(newFavoriteNo + 1);
        collect.setUserId(userIdFromToken);
        collect.setCreateTime(new Date());
        collect.setIsPublic(collect.getIsPublic());
        // 保存收藏夹
        collectService.add(collect);
        return Result.success();
    }

    //将一个视频从一个收藏夹移动到另一个收藏夹
    @LoginRequired
    @RequestMapping("/moveCollect")
    public Result<Void> moveCollect(HttpServletRequest request, Integer videoId, Integer newFavorite){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userIdFromToken);
        collectQuery.setFavorite(newFavorite);
        List<Collect> listByParam = collectService.findListByParam(collectQuery);
        if(listByParam == null || listByParam.isEmpty()){
            return Result.error("目标收藏夹不存在");
        }
        Collect collect = listByParam.get(0);
        // 检查原收藏夹中是否存在该视频
        CollectQuery sourceQuery = new CollectQuery();
        sourceQuery.setUserId(userIdFromToken);
        sourceQuery.setVideoId(videoId);
        Integer sourceCount = collectService.findCountByParam(sourceQuery);
        if (sourceCount == null || sourceCount == 0) {
            return Result.error("原收藏夹中不存在该视频");
        }
        Collect updateCollect = new Collect();
        updateCollect.setFavorite(collect.getFavorite());
        updateCollect.setFavoriteName(collect.getFavoriteName());
        updateCollect.setIsPublic(collect.getIsPublic());
        CollectQuery updateQuery = new CollectQuery();
        updateQuery.setUserId(userIdFromToken);
        updateQuery.setVideoId(videoId);
        collectService.updateByParam(updateCollect,updateQuery);
        return Result.success();
    }

    // 删除收藏夹
    @LoginRequired
    @RequestMapping("/deleteCollect")
    public Result<Void> deleteCollect(HttpServletRequest request, Integer favoriteNO){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userIdFromToken);
        collectQuery.setFavorite(favoriteNO);
        // 检查收藏夹是否存在
        List<Collect> collectList = collectService.findListByParam(collectQuery);
        if(collectList == null || collectList.isEmpty()){
            return Result.error("收藏夹不存在或不属于该用户");
        }
        // 删除收藏夹中的所有收藏记录
        collectService.deleteByParam(collectQuery);
        return Result.success();
    }

    //用户查看其他用户的收藏
    @LoginRequired
    @RequestMapping("/getOtherUserCollect")
    public Result<List<Video>> getOtherUserCollect(Integer userId, Integer favoriteNO, Integer pageNo, Integer pageSize,Integer sortord){
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userId);
        collectQuery.setFavorite(favoriteNO);
        collectQuery.setPageNo(pageNo);
        collectQuery.setPageSize(pageSize);
        List<Video> videoList = new ArrayList();
        List<Collect> collectList=new ArrayList<>();
        if(sortord == 1){
            collectList=collectService.selectOrderByTimeAsc(collectQuery).getList();
        }
        if(sortord == 2){
            collectList=collectService.selectOrderByPlayCount(collectQuery).getList();
        }
        if(sortord == 3){
            collectList=collectService.selectOrderByVideoTime(collectQuery).getList();
        }
        for(Collect collect : collectList){
            Integer videoId = collect.getVideoId();
            // 只有当videoId不为NULL时，才尝试获取视频信息
            if(videoId != null){
                VideoQuery videoQuery = new VideoQuery();
                videoQuery.setVideoId(videoId);
                List<Video> videos = videoService.selectVideo(videoQuery);
                if(videos != null && !videos.isEmpty()){
                    videoList.add(videos.get(0));
                }
            }
        }
        return Result.success(videoList);
    }
    //用户查看其他用户的收藏夹视频数量
    @LoginRequired
    @RequestMapping("/getOtherUserCollectCount")
    public Result<Integer> getOtherUserCollectCount(Integer userId, Integer favoriteNO){
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userId);
        collectQuery.setIsPublic(1);
        collectQuery.setFavorite(favoriteNO);
        // 获取收藏夹的所有记录
        List<Collect> collectList = collectService.findListByParam(collectQuery);
        // 只计算videoId不为NULL的记录
        int validCount = 0;
        for(Collect collect : collectList){
            if(collect.getVideoId() != null){
                validCount++;
            }
        }
        return Result.success(validCount);
    }
    //用户查看其他用户的收藏夹导航栏
    @LoginRequired
    @RequestMapping("/getOtherUserFavorite")
    public Result<List<Map<String, Object>>> getOtherUserFavorite(Integer userId){
        CollectQuery collectQuery = new CollectQuery();
        collectQuery.setUserId(userId);
        collectQuery.setIsPublic(1);
        List<Collect> collectList = collectService.findListByParam(collectQuery);

        // 按favorite_name分组，计算每个收藏夹的视频数量
        Map<String, Collect> favoriteMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        for(Collect collect : collectList){
            String favoriteName = collect.getFavoriteName();
            if(favoriteName != null && !favoriteName.isEmpty()){
                // 如果是新的收藏夹，添加到map中
                if(!favoriteMap.containsKey(favoriteName)){
                    favoriteMap.put(favoriteName, collect);
                    countMap.put(favoriteName, 0);
                }
                // 如果videoId不为NULL，增加计数
                if(collect.getVideoId() != null){
                    countMap.put(favoriteName, countMap.get(favoriteName) + 1);
                }
            }
        }

        // 转换为列表并设置计数
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(Map.Entry<String, Collect> entry : favoriteMap.entrySet()){
            String favoriteName = entry.getKey();
            Collect collect = entry.getValue();
            Integer count = countMap.get(favoriteName);

            Map<String, Object> item = new HashMap<>();
            item.put("favoriteName", favoriteName);
            item.put("favorite", collect.getFavorite());
            item.put("count", count);
            item.put("isPublic", collect.getIsPublic());

            resultList.add(item);
        }

        return Result.success(resultList);
    }
}
