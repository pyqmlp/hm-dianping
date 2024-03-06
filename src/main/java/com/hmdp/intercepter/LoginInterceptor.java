package com.hmdp.intercepter;

import cn.hutool.http.HttpStatus;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.BeanCopyUtils;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author：PanYongQiang
 * @Date：2024/3/6 17:53
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取session
        HttpSession session = request.getSession();
        //2.获取session中的用户
        Object user = session.getAttribute("user");
        //3.判断用户是否存在
        if (user == null) {
            //4.不存在，拦截，返回401状态码
            response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
            return false;
        }


        //5.存在，保存用户信息到Threadlocal
        UserDTO userDTO = BeanCopyUtils.copyBean(user, UserDTO.class);
        UserHolder.saveUser(userDTO);

        //6.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
