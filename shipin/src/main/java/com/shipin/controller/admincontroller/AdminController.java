package com.shipin.controller.admincontroller;

import java.util.List;

import com.shipin.annotation.AdminRequired;
import com.shipin.controller.ABaseController;
import com.shipin.entity.po.User;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.AdminQuery;
import com.shipin.entity.po.Admin;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;
import com.shipin.service.AdminService;
import com.shipin.service.UserService;
import com.shipin.service.VideoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 *  Controller
 */
@RestController("adminController")
@RequestMapping("/api/admin")
public class AdminController extends ABaseController {
    @Resource
    private AdminService adminService;
    @Resource
    private VideoService videoService;
    @Resource
    private UserService userService;

    @Resource
    private com.shipin.service.VideoSearchService videoSearchService;

    @RequestMapping("/login")
    public Result login(@RequestBody AdminQuery query, HttpServletRequest request) {
        Admin admin = adminService.login(query);
        if (admin != null) {
            // 将管理员信息存储到session中
            request.getSession().setAttribute("adminInfo", admin);
            return Result.success(admin);
        } else {
            // 登录失败
            return Result.error(401, "用户名或密码错误");
        }
    }

    @AdminRequired
    @RequestMapping("/logout")
    public Result logout(HttpServletRequest request) {
        // 清除session中的管理员信息
        request.getSession().removeAttribute("adminInfo");
        return Result.success(null);
    }

    @AdminRequired
    @RequestMapping("/getAdminInfo")
    public Result getAdminInfo(HttpServletRequest request) {
        // 从session中获取管理员信息
        Admin adminInfo = (Admin) request.getSession().getAttribute("adminInfo");
        return Result.success(adminInfo);
    }
    //获取所有的视频列表
    @AdminRequired
    @RequestMapping("/getAllAdmin")
    public Result getAllAdmin(@RequestParam Integer pageNo) {
        VideoQuery query = new VideoQuery();
        query.setPageNo(pageNo);
        query.setPageSize(10);
        PaginationResultVO<Video> listByPage = videoService.findListByPage(query);
        for(Video video : listByPage.getList()){
            User user = userService.getUserByUserId(video.getUserId());
            video.setNickName(user.getNickName());
        }
        return Result.success(listByPage);
    }
    //获取所有的用户列表
    @AdminRequired
    @RequestMapping("/getAllUser")
    public Result getAllUser(@RequestParam Integer pageNo) {
        UserQuery query = new UserQuery();
        query.setPageNo(pageNo);
        query.setPageSize(10);
        PaginationResultVO<User> listByPage = userService.findListByPage(query);
        for(User user : listByPage.getList()){
            String avatar = user.getAvatar();
            String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
            user.setAvatar(fullAvatarUrl);
        }
        return Result.success(listByPage);
    }
    //管理员禁用一个用户
    @AdminRequired
    @RequestMapping("/banUser")
    public Result banUser(@RequestParam Integer userId) {
        userService.banUser(userId);
        return Result.success("禁用成功");
    }
    //管理员解禁一个用户
    @AdminRequired
    @RequestMapping("/unbanUser")
    public Result unbanUser(@RequestParam Integer userId) {
        userService.unbanUser(userId);
        return Result.success("解禁成功");
    }

    //全量同步所有视频到 Elasticsearch
    @AdminRequired
    @RequestMapping("/syncAllVideosToEs")
    public Result syncAllVideosToEs() {
        try {
            videoSearchService.syncAllVideosFromDatabase();
            return Result.success("全量同步已完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }
}
