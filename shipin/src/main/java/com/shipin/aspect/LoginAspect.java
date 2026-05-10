package com.shipin.aspect;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.po.User;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.vo.Result;
import com.shipin.mappers.UserMapper;
import com.shipin.utils.JwtUtil;
import com.shipin.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证切面
 * 用于拦截带有@LoginRequired注解的方法并验证登录状态
 */
@Aspect
@Component
public class LoginAspect {

    @Resource
    private JwtUtil jwtUtil;
    
    @Resource
    private UserMapper<User, UserQuery> userMapper;
    
    @Resource
    private RedisUtil redisUtil;

    /**
     * 定义切点：匹配所有带有@LoginRequired注解的方法
     */
    @Pointcut("@annotation(com.shipin.annotation.LoginRequired)")
    public void loginPointcut(){}

    /**
     * 环绕通知：在方法执行前后进行登录验证
     */
    @Around("loginPointcut()")
    public Object aroundLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求信息");
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 检查session中是否有用户信息
        Object userInfo = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userinfo");
        
        // 如果session中没有用户信息，尝试从token中获取
        if (userInfo == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    if (jwtUtil.validateToken(token)) {
                        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
                        
                        // 先尝试从Redis中获取用户信息
                        String redisKey = "user:login:" + userIdFromToken;
                        Object userInfoFromRedis = redisUtil.get(redisKey);
                        
                        if(userInfoFromRedis != null) {
                            // 如果Redis中存在用户信息，直接使用
                            request.getSession().setAttribute("userinfo", userInfoFromRedis);
                            request.getSession().setAttribute("userId", userIdFromToken);
                            
                            // 继续执行原方法
                            return joinPoint.proceed();
                        } else {
                            // 如果Redis中不存在用户信息，从数据库中查询
                            User user = userMapper.selectByUserId(userIdFromToken);
                            
                            if (user != null) {
                                // 构建用户信息
                                Map<String, Object> userInfoMap = new HashMap<>();
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
                                
                                // 将用户信息存储到session中
                                request.getSession().setAttribute("userinfo", userInfoMap);
                                request.getSession().setAttribute("userId", user.getUserId());
                                
                                // 继续执行原方法
                                return joinPoint.proceed();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 验证失败，返回未登录响应
            if (response != null) {
                returnErrorResponse(response, Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "未登录或登录已过期，请重新登录"));
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
		response.setStatus(401); // 设置HTTP状态码为401
		PrintWriter writer = response.getWriter();
		writer.write(String.format("{\"code\":%d,\"msg\":\"%s\"}", result.getCode(), result.getMsg()));
		writer.flush();
		writer.close();
	}
}
