package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.Danmaku;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.DanmakuQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.Result;
import com.shipin.service.AiAgentService;
import com.shipin.service.DanmakuService;
import com.shipin.service.VideoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("DanmakuController")
@RequestMapping("/api/danmaku")
public class DanmakuController {
    @Resource
    private DanmakuService danmakuService;
    @Resource
    private VideoService videoService;

    @Resource
    private AiAgentService aiAgentService;

    //获取一个视频全部弹幕
    @RequestMapping("getDanmaku")
    public Result getDanmaku(@RequestParam("id") Integer video_id) {
        List<Danmaku> danmakus=danmakuService.getDanmakuByVideoId(video_id);
        return Result.success(danmakus);
    }
    //新增弹幕
    @RequestMapping("addDanmaku")
    @LoginRequired
    public Result addDanmaku(@RequestBody Danmaku danmaku) {
        danmakuService.add(danmaku);
        aiAgentService.moderateDanmaku(danmaku);
        return Result.success("发送弹幕成功");
    }
    //删除一条弹幕
    @RequestMapping("deleteDanmaku")
    @LoginRequired
    public Result deleteDanmaku(@RequestParam("id") Integer id,
                                @RequestParam("user_id") Integer user_id,
                                @RequestParam("video_id") Integer video_id) {
        VideoQuery videoQuery=new VideoQuery();
        videoQuery.setVideoId(video_id);
        List<Video> listByParam = videoService.findListByParam(videoQuery);
        if(listByParam.size()==0){
            return Result.error("视频不存在");
        }
        Video video=listByParam.get(0);
        DanmakuQuery dq=new DanmakuQuery();
        dq.setId(id);
        List<Danmaku> danmakus=danmakuService.findListByParam(dq);
        if(danmakus.size()==0){
            return Result.error("弹幕不存在");
        }
        Danmaku danmaku=danmakus.get(0);
        System.out.println(danmaku.getUserId());
        System.out.println(video.getUserId());
        if(!danmaku.getUserId().equals(user_id) && !video.getUserId().equals(user_id)){
            return Result.error("没有权限删除弹幕");
        }
        DanmakuQuery danmakuQuery=new DanmakuQuery();
        danmakuQuery.setId(id);
        danmakuService.deleteByParam(danmakuQuery);
        return Result.success("删除弹幕成功");
    }
    //获取自己在一个视频发送的弹幕
    @RequestMapping("getMyDanmaku")
    @LoginRequired
    public Result getMyDanmaku(@RequestParam("user_id") Integer user_id,
                               @RequestParam("video_id") Integer video_id) {
        DanmakuQuery danmakuQuery=new DanmakuQuery();
        danmakuQuery.setUserId(user_id);
        danmakuQuery.setVideoId(video_id);
        List<Danmaku> danmakus=danmakuService.findListByParam(danmakuQuery);
        return Result.success(danmakus);
    }

    // DPlayer 标准格式 GET 加载弹幕（DPlayer 内部按时间线调度显示）
    @RequestMapping(value = "dplayer/v3", method = RequestMethod.GET)
    public Map<String, Object> dplayerGetDanmaku(@RequestParam("id") Integer video_id) {
        List<Danmaku> danmakus = danmakuService.getDanmakuByVideoId(video_id);
        List<List<Object>> data = new ArrayList<>();
        for (Danmaku d : danmakus) {
            List<Object> item = new ArrayList<>();
            // time: BigDecimal → Double（秒）
            item.add(d.getTime() != null ? d.getTime().doubleValue() : 0);
            // type: 后端 0=顶部,1=滚动 → DPlayer 0=滚动,1=顶部
            int dplayerType = d.getType() != null ? d.getType() : 1;
            if (dplayerType == 0) dplayerType = 1;
            else if (dplayerType == 1) dplayerType = 0;
            item.add(dplayerType);
            // color: hex 字符串
            item.add(d.getColor() != null ? d.getColor() : "#ffffff");
            // author
            item.add(d.getUsername() != null ? d.getUsername() : "匿名用户");
            // text
            item.add(d.getContent() != null ? d.getContent() : "");
            data.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", data);
        return result;
    }

    // DPlayer 默认 POST 发送（前端 danmaku_send 事件负责实际持久化，此处仅返回成功避免 405）
    @RequestMapping(value = "dplayer/v3", method = RequestMethod.POST)
    public Map<String, Object> dplayerSendDanmaku() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        return result;
    }

}
