package com.shipin.controller.webcontroller;

import com.shipin.entity.vo.Result;
import com.shipin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Autowired
    private UserService userService;
    @RequestMapping("/login")
    public Result<?> login(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String checkCode,
                           @RequestParam String checkCodeKey,
                           HttpSession session
                           ) {
        Result<?> result = userService.login(email,password,checkCodeKey,checkCode);
        // 如果登录成功，将用户信息存储到session中
        if (result.getStatus().equals("success")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userinfo = (Map<String, Object>) result.getData();
            session.setAttribute("userinfo", userinfo);
            session.setAttribute("userId", userinfo.get("userId"));
        }
        return result;
    }
    @RequestMapping("/getCheckCode")
    public Result<?> getCheckCode(){
        try {
            Map<String, String> captcha = userService.generateCaptcha();
            return Result.success(captcha);
        }catch (Exception e){
            log.error("获取验证码失败",e);
            return Result.error("获取验证码失败");
        }
    }
    @RequestMapping("/register")
    public Result<?> register(@RequestParam String email,
                              @RequestParam String nickName,
                              @RequestParam String registerPassword,
                              @RequestParam String reRegisterPassword,
                              @RequestParam String checkCode,
                              @RequestParam String checkCodeKey
    ) {
        return userService.register(email,nickName,registerPassword,reRegisterPassword,checkCode,checkCodeKey);
    }
}
