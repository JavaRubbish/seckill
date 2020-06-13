package com.lq.kill.server.service;

import com.lq.kill.model.dto.KillSuccessUserInfo;
import com.lq.kill.model.entity.ItemKillSuccess;
import com.lq.kill.model.mapper.ItemKillSuccessMapper;
import com.lq.kill.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitReceiverService {
    public static final Logger logger  = LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"},containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
        try{
            logger.info("秒杀异步邮件通知-接收消息:{}",info);

            //TODO:真正的发送邮件......
            final String content = String.format(env.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());
            MailDto dto = new MailDto(env.getProperty("mail.kill.item.success.subject"),content,new String[]{info.getEmail()});
//            MailDto dto2 = new MailDto("您有一台iphone11待领取","假的，别信！！！",new String[]{info.getEmail()});
            mailService.sendHTMLEmail(dto);

        }catch(Exception e){
            logger.error("秒杀异步邮件通知-接收消息-发生异常:",e.fillInStackTrace());
        }
    }


    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try{
            logger.info("用户秒杀成功后超时未支付-监听者-接收消息:{}",info);

            //TODO:判断订单状态是否未支付......
            if(info != null){
                ItemKillSuccess entity = itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
                if (entity != null && entity.getStatus().intValue() == 0) {
                    itemKillSuccessMapper.expireOrder(info.getCode());
                }
            }
        }catch(Exception e){
            logger.error("用户秒杀成功后超时未支付-监听者-发生异常:",e.fillInStackTrace());
        }
    }
}
