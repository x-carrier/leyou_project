package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
//启用配置类
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredict")
    public ResponseEntity<Void> accredict(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String token = this.authService.accredict(username,password);
        if (StringUtils.isBlank(token)){
            //身份未认证错误
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60);
        return ResponseEntity.ok(null);
    }

    /**
     * 根据cookie信息获取用户信息
     * @CookieValue 获取cookie信息
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
               @CookieValue("LY_TOKEN") String token,
               HttpServletRequest request,
               HttpServletResponse response
    ){
        try {
            //解析用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            if (userInfo ==  null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            //每解析一次用户信息，就刷新一次时间，这样避免有效时间固定
            //刷新jwt的有效时间
            token = JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            //刷新cookie的有效时间
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
