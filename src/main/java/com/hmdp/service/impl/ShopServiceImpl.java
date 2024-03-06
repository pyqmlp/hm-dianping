package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 判断商铺是否存在
        if (!StrUtil.isBlank(shopJson)) {
            // 存在直接返回
            Shop shop = JSON.parseObject(shopJson, Shop.class);
            return Result.ok(shop);
        }

        // 不存在走mysql查询
        Shop shop = getById(id);
        // 不存在直接返回错误信息
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        // 存在则先更新redis缓存，然后返回
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(shop));
        return Result.ok(shop);
    }
}
