package com.brander.admin.controller;

import com.brander.common.domain.FoManager;
import com.brander.common.domain.JsonResult;
import com.brander.common.enums.JsonResultEnum;
import com.brander.common.exception.JsonException;
import com.brander.common.service.FoManagerService;
import com.brander.common.utils.AchieveUtil;
import com.brander.common.utils.JsonResultUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * 管理员登录
 */
@Controller
@RequestMapping(value = "/admin")
public class LoginController {

    @Autowired
    FoManagerService foManagerService;

    /**导入验证码配置类*/
    @Autowired
    DefaultKaptcha defaultKaptcha;

    /**
    * 管理员登录页面
    * */
    @GetMapping(value = "/login")
    public String login(){
        return "admin/login/index";
    }

    /**
    * 管理员登录提交操作
    * */
    @PostMapping(value = "/loginAction")
    @ResponseBody
    public JsonResult login_action(@RequestParam(value = "username") String username,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "validates") String validates,
                                   HttpServletRequest httpServletRequest) throws Exception{

        String captchaId = (String)httpServletRequest.getSession().getAttribute("vrifyCode");
        if (!captchaId.equals(validates)) {
            throw new JsonException(JsonResultEnum.ADMIN_VALIDATE_ERROR);
        } else {
            //验证成功后返回用户信息对象
            FoManager foManager = foManagerService.loginAction(username,password);
            //更新次数、ip、登录时间
            foManager.setLoginIp(AchieveUtil.getIpAddr(httpServletRequest));
            foManager.setNumber(foManager.getNumber() + 1);
            foManager.setLoginTime(AchieveUtil.getTimeStamp());
            foManagerService.updateByPrimaryKeySelective(foManager);
            //TODO 注册session，记录管理员登录信息
            return JsonResultUtil.success();
        }
    }

    /**
     * 验证码生成方法
     * */
    @GetMapping("/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception{
        byte[] captchaChallengeAsJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String createText = defaultKaptcha.createText();
            httpServletRequest.getSession().setAttribute("vrifyCode", createText);

            //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = defaultKaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

}
