package com.lq.kill.server.service;

import com.lq.kill.server.dto.MailDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@EnableAsync
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Async
    public void sendSimpleEmail(final MailDto dto){
        try{
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(env.getProperty("mail.send.from"));
            message.setTo(dto.getTos());
            message.setSubject(dto.getSubject());
            message.setText(dto.getContent());
            mailSender.send(message);

            logger.info("发送简单文本-发送成功");
        }catch(Exception e){
            logger.error("发送简单文本-发生异常：",e.fillInStackTrace());
        }
    }


    @Async
    public void sendHTMLEmail(final MailDto dto){
        try{

            // 创建多用途邮件消息对象
            MimeMessage mailMessage = mailSender.createMimeMessage();
            // 创建邮件消息助手（参数2：设置为true，表示可以发送超链接、附件）
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setTo(dto.getTos());
            messageHelper.setSubject(dto.getSubject());
            messageHelper.setText(dto.getContent(),true);
            mailSender.send(mailMessage);

            logger.info("发送复杂文本-发送成功");
        }catch(Exception e){
            logger.error("发送复杂文本-发生异常：",e.fillInStackTrace());
        }
    }

}
