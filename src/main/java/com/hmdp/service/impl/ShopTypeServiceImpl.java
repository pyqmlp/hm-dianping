package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        // 从redis中获取分类
        // 存在直接返回
        // 不存在走mysql
        // mysql不存在直接返回错误信息
        // 更新redis缓存
        // 返回数据
        //1.查询redis缓存
        List<String> shopTypeListInRedis = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);

        //2.判断是否存在
        if (!shopTypeListInRedis.isEmpty()) {
            //3.存在就返回
            List<ShopType> shopTypeList = shopTypeListInRedis.stream().map(item -> {
                return JSONUtil.toBean(item, ShopType.class);
            }).collect(Collectors.toList());

            return Result.ok(shopTypeList);
        }

        //4.不存在，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        List<String> resList = shopTypeList.stream().map(JSON::toJSONString).collect(Collectors.toList());

        //5.将查询出来写入redis
        stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, resList);

        //6.将查询出的结果返回
        return Result.ok(shopTypeList);
    }
}
