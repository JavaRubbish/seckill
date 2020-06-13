package com.lq.kill.server.service;

import com.lq.kill.model.entity.ItemKillSuccess;
import com.lq.kill.model.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private Environment env;

    @Scheduled(cron = "* 0/30 * * * ? ")
    public void schedulerExpireOrders(){
        try{
            List<ItemKillSuccess> list = itemKillSuccessMapper.selectExpireOrders();
//            for (ItemKillSuccess entity : list) {
//                logger.info("获取当前记录：{}",entity);
//            }

            if (list != null && !list.isEmpty()){
                //lambda java8写法
                list.stream().forEach(i -> {
                    if (i != null && i.getDifferTime() > env.getProperty("scheduler.expire.orders.time",Integer.class)){
                        itemKillSuccessMapper.expireOrder(i.getCode());
                    }
                });
            }
        }catch(Exception e){
            logger.error("定时获取status=0的订单是否超过TTL，然后失效-发生异常：",e.fillInStackTrace());
        }
    }

    @Scheduled(cron = "* 0/30 * * * ?")
    public void schedulerExpireOrdersV2(){
        logger.info("V2的定时任务------");
    }

    @Scheduled(cron = "* 0/30 * * * ?")
    public void schedulerExpireOrdersV3(){
        logger.info("V3的定时任务------");
    }
}
