package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter{

    @Autowired
    private JwtProperties jwtProperties;

    private static ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        //解析token，获取用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        //将用户信息保存到ThreadLocal中，每一个线程只有一个ThreadLocal
        if (userInfo == null){
            return false;
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //默认线程结束清除线程里的变量，但这里是线程池里的线程，知道服务器关闭才结束，所以要手动清除
        THREAD_LOCAL.remove();
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

}
