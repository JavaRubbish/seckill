package com.lq.kill.server.controller;

import com.lq.kill.api.enums.StatusCode;
import com.lq.kill.api.response.BaseResponse;
import com.lq.kill.model.dto.KillSuccessUserInfo;
import com.lq.kill.model.mapper.ItemKillSuccessMapper;
import com.lq.kill.server.dto.KillDto;
import com.lq.kill.server.service.KillService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class KillController {
    private static final Logger logger = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    @Autowired
    private KillService killService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;


    /**
     * 商品秒杀核心业务逻辑
     * @param killDto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix + "/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result, HttpSession session){
        if(result.hasErrors() || killDto.getKillId() <= 0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            return new BaseResponse(StatusCode.UserNotLogin);
        }
        Integer userId = (Integer) uid;
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            Boolean res = killService.killItem(killDto.getKillId(), userId);
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈～商品已抢购一空或者不再抢购时间段哦！");
            }
        }catch (Exception e){
            response = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }

    /**
     * 商品秒杀核心业务逻辑---用于jmeter压力测试
     * @param killDto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix + "/execute/lock",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeLock(@RequestBody @Validated KillDto killDto, BindingResult result){
        if(result.hasErrors() || killDto.getKillId() <= 0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            Boolean res = killService.killItemV4(killDto.getKillId(), killDto.getUserId());
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈～商品已抢购一空或者不再抢购时间段哦！");
            }
        }catch (Exception e){
            response = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }



    /**
     * 查看秒杀订单详情
     * @param orderNo
     * @param modelMap
     * @return
     */
    @RequestMapping(value = prefix + "/record/detail/{orderNo}",method = RequestMethod.GET)
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap){
        if (StringUtils.isBlank(orderNo)) {
            return "error";
        }
        KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
        if (info == null) {
            return "error";
        }
        modelMap.put("info",info);
        return "killRecord";
    }



    //抢购成功跳转页面
    @RequestMapping(value = prefix +"/execute/success",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    //抢购失败跳转页面
    @RequestMapping(value = prefix + "execute/fail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }
}
