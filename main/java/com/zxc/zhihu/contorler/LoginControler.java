package com.zxc.zhihu.contorler;

import com.zxc.zhihu.async.EventModel;
import com.zxc.zhihu.async.EventProducer;
import com.zxc.zhihu.async.EventType;
import com.zxc.zhihu.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author zxc 用户登录功能
 * @date 2021
 */
@Controller
public class LoginControler {

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    //注册用户功能
    @RequestMapping(value = "/reg/", method = RequestMethod.POST)
    public String reg(@RequestParam("username") String username, @RequestParam("password") String password, Model model,
                      HttpServletResponse response, @RequestParam(value = "next", required = false) String next) {

        try {
            //register是登录的Map集合
            //注册失败 key:msg value:提示信息
            //注册成功 key:ticket value:令牌码ticket
            Map<String, String> register = userService.register(username, password);
            if (register.containsKey("ticket")) {
                //创建cookie对象 key:ticket value:令牌码ticket
                Cookie cookie = new Cookie("ticket", register.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);

                if (StringUtils.isNotBlank(next)) {//next 应该是指定跳转
                    return "redirect:/" + next;
                }
                return "redirect:/"; //注册成功，重定向到首页
            } else {
                model.addAttribute("msg", register.get("msg"));//与前端交互的错误提示信息
                return "login";
            }

        } catch (Exception e) {

            return "login"; //注册失败
        }
    }

    /**
     * 用户登陆功能
     *
     * @param username
     * @param password
     * @param model
     * @param rememberme
     * @param response
     * @param next
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, Model model,
                        @RequestParam("rememberme") String rememberme,
                        HttpServletResponse response, @RequestParam(value = "next", required = false) String next) {

        try {
            //若登录成功 返回的是map集合 map集合中有两个元素 1.key:ticket value:ticket码 2.key:userid value:userid(String)
            Map<String, String> login = userService.login(username, password);
            if (login.containsKey("ticket")) {
                //设置cookie key:ticket value:ticket码
                Cookie cookie = new Cookie("ticket", login.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);


                //用户登录完成以后，这里经过检查，发现用户ip异常，发送一个邮件
                // 用户ip异常，具体是什么表现？
                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
                        .setExt("username", username).setExt("email", "1036795393@qq.com")//845713694@qq
                        .setActorId(Integer.parseInt(login.get("userId"))));


                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/"; //注册成功，重定向到首页
            } else {
                model.addAttribute("msg", login.get("msg"));//与前端交互的错误提示信息
                return "login";
            }

        } catch (Exception e) {

            return "login"; //注册失败
        }
    }

    /**
     * 不符合拦截器的过滤条件 跳转过来的
     *
     * @param next
     * @param model
     * @return
     */
    @RequestMapping(value = "/reglogin", method = RequestMethod.GET)
    public String reglogin(@RequestParam(value = "next", required = false) String next, Model model) {
        model.addAttribute("next", next);
        return "login"; //注册失败
    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }
}
