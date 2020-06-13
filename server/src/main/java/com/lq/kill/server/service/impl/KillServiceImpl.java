package com.lq.kill.server.service.impl;

import com.lq.kill.model.entity.ItemKill;
import com.lq.kill.model.entity.ItemKillSuccess;
import com.lq.kill.model.mapper.ItemKillMapper;
import com.lq.kill.model.mapper.ItemKillSuccessMapper;
import com.lq.kill.server.enums.SysConstant;
import com.lq.kill.server.service.KillService;
import com.lq.kill.server.service.RabbitSenderService;
import com.lq.kill.server.utils.RandomUtil;
import com.lq.kill.server.utils.SnowFlake;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KillServiceImpl implements KillService {

    private static final Logger logger = LoggerFactory.getLogger(KillServiceImpl.class);

    private SnowFlake snowFlake = new SnowFlake(2,3);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    ItemKillMapper itemKillMapper;

    @Autowired
    RabbitSenderService rabbitSenderService;


    /**
     * 商品秒杀核心业务逻辑处理
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        Boolean result = false;
        //TODO:判断用户是否已经成功抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //TODO:查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectById(killId);

            //TODO:查询收是否可以被秒杀 cankill = 1 ?
            if (itemKill != null && 1 == itemKill.getCanKill()){
                //TODO:扣减库存
                int res = itemKillMapper.updateKillItem(killId);

                //TODO:库存是否扣减成功？是-->生成秒杀成功订单，同时通知用户秒杀成功的消息
                if(res > 0){
                    commonRecordKillSuccessInfo(itemKill,userId);

                    result = true;
                }
            }
        }else {
            throw new Exception("您已经抢购过该商品了!");
        }

        return result;
    }


    /**
     * 通用的方法-记录用户秒杀成功后生成的订单，并进行异步邮件消息的通知
     * @param itemKill
     * @param userId
     * @throws Exception
     */
    private void commonRecordKillSuccessInfo(ItemKill itemKill,Integer userId) throws Exception{
        //TODO:秒杀成功后生成的订单记录
        ItemKillSuccess entity = new ItemKillSuccess();
        String orderNo = String.valueOf(snowFlake.nextId());

        //entity.setCode(RandomUtil.generateOrderCode()); //传统时间戳+N位随机数
        entity.setCode(orderNo); //雪花算法
        entity.setItemId(itemKill.getItemId());
        entity.setKillId(itemKill.getId());
        entity.setUserId(userId.toString());
        entity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        entity.setCreateTime(DateTime.now().toDate());
        //TODO:学以致用，举一反三 -> 仿照单例模式的双重检验锁写法
        if (itemKillSuccessMapper.countByKillUserId(itemKill.getId(),userId) <= 0){
            int res=itemKillSuccessMapper.insertSelective(entity);

            if (res>0){
                //TODO:进行异步邮件消息的通知=rabbitmq+mail
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);

                //TODO:入死信队列，用于 “失效” 超过指定的TTL时间时仍然未支付的订单
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
            }
        }
    }

    /**
     * 版本2-mysql层面优化并发，效果一般
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        //TODO:判断当前用户是否已经抢购过当前商品
        if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
            //TODO:查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);

            //TODO:判断是否可以被秒杀canKill=1？
            if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                //TODo:扣减库存
                int res = itemKillMapper.updateKillItemV2(killId);

                //TODO:扣减是否成功？是则生成秒杀订单，同时通知用户秒杀成功的消息
                if (res > 0) {
                    commonRecordKillSuccessInfo(itemKill, userId);

                    result = true;
                }
            }
        }
        else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }



    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 版本3-redis分布式锁控制并发，效果很好
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        //TODO:判断当前用户是否已经抢购过当前商品
        if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {

            //TODO:借助redis的原子操作实现分布式锁，对共享资源进行控制
            ValueOperations valueOperations = stringRedisTemplate.opsForValue();
            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value = RandomUtil.generateOrderCode();
            Boolean cacheRes = valueOperations.setIfAbsent(key, value);

            if (cacheRes) {
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try{
                    //TODO:查询待秒杀商品详情
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    //TODO:判断是否可以被秒杀canKill=1？
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        //TODo:扣减库存
                        int res = itemKillMapper.updateKillItemV2(killId);

                        //TODO:扣减是否成功？是则生成秒杀订单，同时通知用户秒杀成功的消息
                        if (res > 0) {
                            commonRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    }
                }catch (Exception e){
                        throw new Exception("还没到抢购日期、已过了抢购时间或已抢购完毕!");
                }finally {
                    //只释放自己的锁，不会影响别人的锁
                    if (value.equals(valueOperations.get(key).toString())) {
                        stringRedisTemplate.delete(key);
                    }
                }
            }
        }
        else{
            throw new Exception("Redis--您已经抢购过该商品了!");
        }
        return result;
    }


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 版本4-redisson分布式锁处理并发，效果很好
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        final String lockKey = new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            Boolean cacheRes = lock.tryLock(30, 10, TimeUnit.SECONDS);
            if (cacheRes) {
                //TODO:判断当前用户是否已经抢购过当前商品
                if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
                    //TODO:查询待秒杀商品详情
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        int res = itemKillMapper.updateKillItemV2(killId);

                        //TODO:扣减是否成功？是则生成秒杀订单，同时通知用户秒杀成功的消息
                        if (res > 0) {
                            commonRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    }
                    } else {
                        throw new Exception("Reddison-您已经抢购过该商品了!");
                    }
                }
            }
        finally {
                    lock.unlock();
                    //lock.forceUnlock();
                }
            return result;
        }




     @Autowired
     private CuratorFramework curatorFramework;

    private static final String pathPrefix = "/kill/zkLock";

    /**
     * 版本5-Zookeeper控制并发，效果很好
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, pathPrefix + killId + userId + "-lock");
        try{
            if (mutex.acquire(10L, TimeUnit.SECONDS)) {
                //TODO:判断用户是否已经成功抢购过当前商品
                if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectById(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill()){
                        int res = itemKillMapper.updateKillItem(killId);
                        if(res > 0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result = true;
                        }
                    }
                }else {
                    throw new Exception("您已经抢购过该商品了!");
                }
            }
        }catch(Exception e){
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕!");
        }finally{
            if (mutex != null) {
                mutex.release();
            }
        }
        return result;
    }
}
