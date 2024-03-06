package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.BeanCopyUtils;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号码
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("请输入正确的手机号码");
        }

        // 2.校验验证码
        String code = loginForm.getCode();
        if (StrUtil.isBlank(code) && code.equals(session.getAttribute("code"))) {
            return Result.fail("验证码错误");
        }
        String phone = loginForm.getPhone();
        User user = query().eq("phone", phone).one();

        // 3.判断用户是否存在
        if (user == null) {
            // 3.1.用户不存在则创建用户
            user = createUserWithPhone(phone);
        }
        UserDTO userDTO = BeanCopyUtils.copyBean(user, UserDTO.class);
        // 4.保存用户信息到session
        session.setAttribute("user", userDTO);

        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        save(user);
        return user;
    }
}
