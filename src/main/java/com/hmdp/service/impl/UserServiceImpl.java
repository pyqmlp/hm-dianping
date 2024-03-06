package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.BeanCopyUtils;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(LoginFormDTO loginForm) {
        // 1.校验手机号码
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("请输入正确的手机号码");
        }
        // 从redis中获取验证码
        String redisCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        // 2.校验验证码
        String code = loginForm.getCode();
        if (StrUtil.isBlank(code) && code.equals(redisCode)) {
            return Result.fail("验证码错误");
        }

        User user = query().eq("phone", phone).one();

        // 3.判断用户是否存在
        if (user == null) {
            // 3.1.用户不存在则创建用户
            user = createUserWithPhone(phone);
        }
        UserDTO userDTO = BeanCopyUtils.copyBean(user, UserDTO.class);
        // 生成token
        String token = UUID.randomUUID().toString(true);

        // 将UserDTO转为Map
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .ignoreNullValue() // 简化null值忽略的设置
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue != null ? fieldValue.toString() : null)
        );


        // 4.保存用户信息到redis中
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 如果redis中的code没有过期则在登录成功时将code手动过期
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisConstants.LOGIN_CODE_KEY + phone))) {
            // 删除code
            stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        }

        // 返回token
        return Result.ok(token);
    }

    @Override
    public Result sendCode(String phone) {
        // 1.校验手机号码
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，直接返回错误信息
            return Result.fail("请输入正确的手机号码");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.将验证码保存到redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // todo 5.发送验证码
        log.info("发送验证码成功，验证码为：{}", code);
        // 6.返回结果
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        save(user);
        return user;
    }
}
