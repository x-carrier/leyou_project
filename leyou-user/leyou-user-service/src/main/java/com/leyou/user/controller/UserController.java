package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 检验数据是否可用
     * @param date
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(@PathVariable("data")String date, @PathVariable("type") Integer type){
        Boolean bool = this.userService.checkUser(date,type);
        if (bool == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bool);
    }

    /**
     * 发送短信验证码
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sentVerifyCode(@RequestParam("phone") String phone){
        this.userService.sentVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("register")
    public ResponseEntity<Void> userRegister(@Valid User user, @RequestParam("code") String code){
        Boolean flag = this.userService.userRegister(user, code);
        if (!flag){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询用户信息
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username,@RequestParam("password") String password){
        User user = this.userService.queryUser(username,password);
        if (user == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }

}
