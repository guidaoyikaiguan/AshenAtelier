package com.shipin.aspect;

import com.shipin.annotation.AdminRequired;
import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.vo.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 管理员登录验证切面
 * 用于拦截带有@AdminRequired注解的方法并验证管理员登录状态
 */
@Aspect
@Component
public class AdminLoginAspect {

    /**
     * 定义切点：匹配所有带有@AdminRequired注解的方法
     */
    @Pointcut("@annotation(com.shipin.annotation.AdminRequired)")
    public void adminLoginPointcut() {}

    /**
     * 环绕通知：在方法执行前后进行管理员登录验证
     */
    @Around("adminLoginPointcut()")
    public Object aroundAdminLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求信息");
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 检查session中是否有管理员信息
        Object adminInfo = request.getSession(false) == null ? null : request.getSession(false).getAttribute("adminInfo");

        // 如果session中没有管理员信息，返回未登录响应
        if (adminInfo == null) {
            // 验证失败，返回未登录响应
            if (response != null) {
                returnErrorResponse(response, Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "管理员未登录或登录已过期，请重新登录"));
            }
            return null;
        }

        // 已登录，继续执行原方法
        return joinPoint.proceed();
    }

    /**
     * 返回错误响应
     */
    private void returnErrorResponse(HttpServletResponse response, Result<?> result) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(String.format("{\"code\":%d,\"msg\":\"%s\"}", result.getCode(), result.getMsg()));
        writer.flush();
        writer.close();
    }
}
