package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.Compilations;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.CompilationsQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.Result;
import com.shipin.service.CompilationsService;
import com.shipin.service.VideoService;
import com.shipin.utils.JwtUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController("CompilationsController")
@RequestMapping("/api/compilations")
public class CompilationsController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private CompilationsService compilationsService;
    @Resource
    private VideoService videoService;
    //用户获取合集中的视频
    @LoginRequired
    @RequestMapping("/getCompilations")
    public Result getCompilations(Integer userId,HttpServletRequest request,Integer compilationsId,Integer pageNo) {
        List<Video> videoList=new ArrayList<>();
        
        // 根据pageNo决定返回的数据量：第一页7条，其他页6条
        
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userId);
        compilationsQuery.setCompilationsId(compilationsId);
        compilationsQuery.setPageNo(pageNo);
        compilationsQuery.setPageSize(6);
        
        // 查询合集中的记录
        List<Compilations> compilationsList= compilationsService.selectCompilationsByPage(compilationsQuery).getList();

        // 遍历合集中的记录，只添加videoId不为null的视频，最多添加pageSize个
        for(Compilations compilations:compilationsList){
            Integer videoId = compilations.getVideoId();
            if(videoId!=null){
                VideoQuery videoQuery= new VideoQuery();
                videoQuery.setVideoId(videoId);
                List<Video> videos=videoService.selectVideo(videoQuery);
                if(videos!= null && !videos.isEmpty()){
                    videoList.add(videos.get(0));
                }
            }
        }
        return Result.success(videoList);
    }
    //获取当前合集有多少视频
    @LoginRequired
    @RequestMapping("/getCompilationsCount")
    public Result getCompilationsCount(Integer userId,Integer compilationsId,HttpServletRequest request) {
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userId);
        compilationsQuery.setCompilationsId(compilationsId);
        List<Compilations> compilationsList=compilationsService.findListByParam(compilationsQuery);
        int validCount = 0;
        for(Compilations compilations:compilationsList){
            if(compilations.getVideoId()!=null){
                validCount++;
            }
        }
        return Result.success(validCount);
    }
    //用户获取左侧导航栏合集列表
    @LoginRequired
    @RequestMapping("/getCompilationsList")
    public Result getCompilationsList(Integer userId,HttpServletRequest request) {
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userId);
        List<Compilations> compilationsList= compilationsService.findListByParam(compilationsQuery);

        Map<String, Compilations> compilationsMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        for(Compilations compilations : compilationsList){
            String compilationsName = compilations.getCompilationsName();
            if(compilationsName != null && !compilationsName.isEmpty()){
                // 如果是新的收藏夹，添加到map中
                if(!compilationsMap.containsKey(compilationsName)){
                    compilationsMap.put(compilationsName, compilations);
                    countMap.put(compilationsName, 0);
                }
                // 如果videoId不为NULL，增加计数
                if(compilations.getVideoId() != null){
                    countMap.put(compilationsName, countMap.get(compilationsName) + 1);
                }
            }
        }

        // 转换为列表并设置计数
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(Map.Entry<String, Compilations> entry : compilationsMap.entrySet()){
            String compilationsName = entry.getKey();
            Compilations compilations = entry.getValue();
            Integer count = countMap.get(compilationsName);

            Map<String, Object> item = new HashMap<>();
            item.put("compilationsName", compilationsName);
            item.put("compilations", compilations.getCompilationsId());
            item.put("count", count);
            resultList.add(item);
        }
        return Result.success(resultList);
    }
    //修改合集中的名称
    @LoginRequired
    @RequestMapping("/updateCompilationsName")
    public Result updateCompilationsName(HttpServletRequest request,Integer compilationsId,String compilationsName) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userIdFromToken);
        compilationsQuery.setCompilationsId(compilationsId);
        Compilations compilationsBean=new Compilations();
        compilationsBean.setCompilationsName(compilationsName);
        compilationsService.updateByParam(compilationsBean,compilationsQuery);
        return Result.success();
    }
    //新增合集
    @LoginRequired
    @RequestMapping("/addCompilations")
    public Result addCompilations(HttpServletRequest request,@RequestBody Compilations compilations) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        // 参数校验
        if (compilations.getCompilationsName() == null || compilations.getCompilationsName().trim().isEmpty()) {
            return Result.error("收藏夹名称不能为空");
        }
        // 获取当前用户最新的收藏夹编号
        Integer newCompilationsNo = compilationsService.selectCompilationsNo(userIdFromToken);
        compilations.setCompilationsId( newCompilationsNo+ 1);
        compilations.setUserId(userIdFromToken);
        compilationsService.add(compilations);
        return Result.success("新增成功");
    }
    //删除合集
    @LoginRequired
    @RequestMapping("/deleteCompilations")
    public Result deleteCompilations(HttpServletRequest request,Integer compilationsId) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userIdFromToken);
        compilationsQuery.setCompilationsId(compilationsId);
        // 检查收藏夹是否存在
        List<Compilations> compilationsList = compilationsService.findListByParam(compilationsQuery);
        if(compilationsList == null || compilationsList.isEmpty()){
            return Result.error("收藏夹不存在或不属于该用户");
        }
        // 删除收藏夹中的所有收藏记录
        compilationsService.deleteByParam(compilationsQuery);
        return Result.success();
    }
    //删除合集中的一个视频
    @LoginRequired
    @RequestMapping("/deleteCompilationsVideo")
    public Result deleteCompilationsVideo(HttpServletRequest request,Integer compilationsId,Integer videoId) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        CompilationsQuery compilationsQuery = new CompilationsQuery();
        compilationsQuery.setUserId(userIdFromToken);
        compilationsQuery.setCompilationsId(compilationsId);
        compilationsQuery.setVideoId(videoId);
        compilationsService.deleteByParam(compilationsQuery);
        return Result.success();
    }
}
