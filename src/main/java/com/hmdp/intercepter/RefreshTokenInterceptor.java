package com.hmdp.intercepter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author：PanYongQiang
 * @Date：2024/3/6 17:53
 */
@AllArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //1.获取请求头中的token
        String token = request.getHeader("authorization");

        String userKey = RedisConstants.LOGIN_USER_KEY + token;

        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 不为空，从redis中获取用户信息
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(userKey);
        if (userMap.isEmpty()) {

            return true;
        }

        // 将map转换为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 将用户信息放入ThreadLocal中
        UserHolder.saveUser(userDTO);

        // 刷新token有效期
        stringRedisTemplate.expire(userKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        //6.放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
