package com.shipin.config;

import com.shipin.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.root:D:/shipin}")
    private String storageRoot;

    @Autowired
    private LoginInterceptor loginInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 已经改用AOP实现登录验证，注释掉拦截器配置
        // registry.addInterceptor(loginInterceptor)
        //         .addPathPatterns("/api/**") // 拦截所有 API 请求
        //         .excludePathPatterns(
        //                 "/api/user/login", "/api/user/register", "/api/user/getCheckCode", // 用户相关的公开接口
        //                 "/api/video/loadRecommendVideo", "/api/video/loadVideo", // 视频相关的公开接口
        //                 "/api/category/**" // 分类相关的公开接口
        //         ); // 排除不需要登录的请求
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + storageRoot + "/avatar/");
        registry.addResourceHandler("/video/**")
                .addResourceLocations("file:" + storageRoot + "/video/");
        registry.addResourceHandler("/cover/**")
                .addResourceLocations("file:" + storageRoot + "/cover/");
    }
}