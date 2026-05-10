package com.shipin.interceptor;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.vo.Result;
import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.po.User;
import com.shipin.entity.query.UserQuery;
import com.shipin.mappers.UserMapper;
import com.shipin.utils.JwtUtil;
import com.shipin.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证拦截器
 * 用于拦截带有 @LoginRequired 注解的请求并验证登录状态
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    
    @Resource
    private JwtUtil jwtUtil;
    
    @Resource
    private UserMapper<User, UserQuery> userMapper;
    
    @Resource
    private RedisUtil redisUtil;

    public LoginInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查请求的处理器是否是方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            
            // 检查方法是否带有 @LoginRequired 注解
            if (method.isAnnotationPresent(LoginRequired.class)) {
                // 检查 session 中是否有用户信息
                Object userInfo = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userinfo");
                
                // 如果session中没有用户信息，尝试从token中获取
                if (userInfo == null) {
                    String token = request.getHeader("Authorization");
                    if(token!=null && token.startsWith("Bearer ")){
                        token = token.substring(7);
                        try {
                            if(jwtUtil.validateToken(token)){
                                Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
                                
                                // 先尝试从Redis中获取用户信息
                                String redisKey = "user:login:" + userIdFromToken;
                                Object userInfoFromRedis = redisUtil.get(redisKey);
                                
                                if(userInfoFromRedis != null) {
                                    // 如果Redis中存在用户信息，直接使用
                                    request.getSession().setAttribute("userinfo", userInfoFromRedis);
                                    request.getSession().setAttribute("userId", userIdFromToken);
                                    return true;
                                } else {
                                    // 如果Redis中不存在用户信息，从数据库中查询
                                    User user = userMapper.selectByUserId(userIdFromToken);
                                    if(user!=null){
                                        HashMap<Object, Object> userInfoMap = new HashMap<>();
                                        userInfoMap.put("userId", user.getUserId());
                                        userInfoMap.put("email", user.getEmail());
                                        userInfoMap.put("nickName", user.getNickName());
                                        // 返回完整的头像URL
                                        String avatar = user.getAvatar();
                                        String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
                                        userInfoMap.put("avatar", fullAvatarUrl);
                                        userInfoMap.put("createTime", user.getCreateTime());
                                        userInfoMap.put("myCoin", user.getMyCoin());
                                        
                                        // 存储到Redis中，过期时间与token一致
                                        redisUtil.set(redisKey, userInfoMap, jwtUtil.getExpireTime() / 1000);
                                        
                                        // 存储到session中
                                        request.getSession().setAttribute("userinfo", userInfoMap);
                                        request.getSession().setAttribute("userId", user.getUserId());
                                        return true;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // token验证过程中出现异常
                            e.printStackTrace();
                        }
                    }
                    // 只有在确实需要登录但未提供有效凭证时才返回401
                    returnErrorResponse(response, Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "未登录或登录已过期，请重新登录"));
                    return false;
                }
            }
        }
        // 放行请求
        return true;
    }

    /**
     * 返回错误响应
     */
    private final void returnErrorResponse(HttpServletResponse response, Result<?> result) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
