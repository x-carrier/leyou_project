package com.leyou.user.service;

import com.leyou.user.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //设置前缀
    private static final String KEY_PREFIX="user:verify:";

    /**
     * 校验数据是否可用
     * @param date
     * @param type
     * @return
     */
    public Boolean checkUser(String date, Integer type) {
        User recode = new User();
        if (type == 1){
            recode.setUsername(date);
        }else if (type == 2){
            recode.setPhone(date);
        }else {
            return null;
        }
        int i = this.userMapper.selectCount(recode);
        return i == 0;
    }

    /**
     * 发送短信验证码
     * @param phone
     */
    public void sentVerifyCode(String phone) {
        //判断phone是否为空
        if (StringUtils.isBlank(phone)){
            return;
        }

        //生成验证码
        String code = NumberUtils.generateCode(6);
        //发送消息到rabbitMq
        Map<String,String> msg = new HashMap();
        msg.put("phone",phone);
        msg.put("code",code);
        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE","verifycode.sms",msg);
        //吧验证码保存到redis
        this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);
    }

    public Boolean userRegister(User user, String code) {
        //查询redis中的验证码
        String redis_code = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        //检验验证码
        if (!StringUtils.equals(code,redis_code)){
            return false;
        }
        //生成salt
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加salt加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setId(null);
        user.setCreated(new Date());
        //新增用户
        this.userMapper.insertSelective(user);

        //删除验证码
        redisTemplate.delete(code);

        return true;

    }

    /**
     * 查询用户信息
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
        User recode = new User();
        recode.setUsername(username);
        User query_user = this.userMapper.selectOne(recode);
        //判断查询结果是否为空
        if (query_user ==  null){
            return null;
        }
        //获取salt,对用户输入的密码进行加salt
        String salt = query_user.getSalt();
        password = CodecUtils.md5Hex(password,salt);

        if (!StringUtils.equals(password,query_user.getPassword())){
            //和查询到的密码不一致，验证失败
            return null;
        }

        return query_user;
    }
}
