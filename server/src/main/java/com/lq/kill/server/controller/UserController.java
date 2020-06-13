package com.lq.kill.server.controller;

import com.lq.kill.model.entity.User;
import com.lq.kill.model.mapper.UserMapper;
import jodd.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private Environment env;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = {"/to/login","/unauth"})
    public String toLogin(){
        return "login";
    }

    @RequestMapping(value = {"/to/register","/unauth"})
    public String toRegister(){
        return "register";
    }

    /**
     * 用户登陆
     * @param userName
     * @param password
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(@RequestParam String userName, @RequestParam String password, ModelMap modelMap){
        String errorMsg = "";
        try{
            if (!SecurityUtils.getSubject().isAuthenticated()) {
                String newPsd = new Md5Hash(password,env.getProperty("shiro.encrypt.password.salt")).toString();
                UsernamePasswordToken token = new UsernamePasswordToken(userName, newPsd);
                SecurityUtils.getSubject().login(token);
            }
        }catch (UnknownAccountException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (DisabledAccountException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (IncorrectCredentialsException e){
            errorMsg = e.getMessage();
            modelMap.addAttribute("userName",userName);
        }
        catch (Exception e){
            errorMsg = "用户登陆异常，请联系管理员!";
            e.printStackTrace();
        }
        if (StringUtil.isBlank(errorMsg)) {
            return "redirect:/index";
        }else{
            modelMap.addAttribute("errorMsg",errorMsg);
            return "login";
        }
    }

    /**
     * 注销
     * @return
     */
    @RequestMapping(value = "/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "login";
    }

    /**
     * 用户注册
     * @return
     */
    @RequestMapping(value = "/signup",method = RequestMethod.POST)
    public String signup(HttpServletRequest request,ModelMap modelMap){
        String msg = "";
        String userName = request.getParameter("userName");
//        if (userName == "") {
//            msg = "用户名不能为空!";
//            modelMap.addAttribute("msg",msg);
//            return "register";
//        }
        User exist_user = userMapper.selectByUserName(userName);
        if (exist_user != null) {
            msg = "该用户已存在!";
            modelMap.addAttribute("userName",userName);
            modelMap.addAttribute("msg",msg);
            return "register";
        }
        User user = new User();
        user.setUserName(userName);
        String npwd = new Md5Hash(request.getParameter("password"),env.getProperty("shiro.encrypt.password.salt")).toString();
        user.setPassword(npwd);
        user.setPhone(request.getParameter("phone"));
        user.setEmail(request.getParameter("email"));
        user.setIsActive((byte)1);
        userMapper.insert(user);
        msg = "注册成功!";
        modelMap.addAttribute("msg",msg);
        return "register";
    }
}
