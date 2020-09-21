package com.leyou.geteway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.geteway.config.FilterProperties;
import com.leyou.geteway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
   private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        //前置过滤
        return "pre";
    }

    @Override
    public int filterOrder() {
        //执行优先级（需可扩展）
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        //获取白名单
        List<String> allowPaths = filterProperties.getAllowPaths();
        //初始化zuul网关的运行上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取请求参数
        HttpServletRequest request = currentContext.getRequest();
        //获取当前请求路径
        String url = request.getRequestURL().toString();
        //判断是否包含在白名单中
        for (String allowPath : allowPaths) {
            boolean contains = StringUtils.contains(url, allowPath);
            if (contains){
                return false;
            }
        }
        return true;
    }

    /**
     * 拦截业务功能
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //初始化zuul网关的运行上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取请求参数
        HttpServletRequest request = currentContext.getRequest();

        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        if (StringUtils.isBlank(token)){
            //拒绝转发请求
            currentContext.setResponseGZipped(false);
            //设置响应值
            currentContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        //解析token
        try {
            JwtUtils.getInfoFromToken(token,jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            //拒绝转发请求
            currentContext.setResponseGZipped(false);
            //设置响应值
            currentContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        return null;
    }
}
