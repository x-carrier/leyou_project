package com.leyou.geteway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 解决服务器跨域问题
 */
@Configuration
public class LeyouCorsConfiguration {


    @Bean
    public CorsFilter corsFilter(){

        //cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //配置那四个头信息
        /*Access-Control-Allow-Origin: http://manage.leyou.com
        Access-Control-Allow-Credentials: true
        Access-Control-Allow-Methods: GET, POST, PUT
        Access-Control-Allow-Headers: X-Custom-Header*/
//        允许跨域的域名，如果要携带cookie就不能写*，*代表所有域名都可以跨域访问
        corsConfiguration.addAllowedOrigin("http://root.xiaohong.com");
        corsConfiguration.addAllowedOrigin("http://www.xiaohong.com");
//        允许携带cookie信息
        corsConfiguration.setAllowCredentials(true);
//        需要跨域的请求方法,*代表所有请求方法都能跨域
        corsConfiguration.addAllowedMethod("*");
//        允许携带如何头信息
        corsConfiguration.addAllowedHeader("*");
        //cors配置源对象
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        //参数1：需要校验的路径  参数2：cors配置对象
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        //返回corsFileter实例，参数为cors配置源对象
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
