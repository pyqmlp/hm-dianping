package com.hmdp.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author：PanYongQiang
 * @Date：2024/3/6 20:12
 */
public class BeanCopyUtils {

    public static <T> T copyBean(Object source, Class<T> targetClass) {
        // 创建返回对象
        T target = null;
        try {
            target = targetClass.getConstructor().newInstance(); //获取空参构造实例
            BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    public static <T, V> List<T> copyBeanList(List<V> sourceList, Class<T> targetClass) {
        return sourceList.stream()
                .map(o -> copyBean(o, targetClass))
                .collect(Collectors.toList());
    }

}
