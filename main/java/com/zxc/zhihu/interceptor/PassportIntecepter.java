package com.zxc.zhihu.interceptor;

import com.zxc.zhihu.dao.UserMapper;
import com.zxc.zhihu.dao.loginTicketMapper;
import com.zxc.zhihu.model.HostHolder;
import com.zxc.zhihu.model.User;
import com.zxc.zhihu.model.loginTicket;
import com.zxc.zhihu.model.loginTicketExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 使用拦截器interceptor来拦截所有用户请求
 *
 * @author zxc
 * @date 2021
 */
@Component
public class PassportIntecepter implements HandlerInterceptor {
    @Autowired
    private loginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 在所有的请求开始之前 都会请求客户端的cookie 查询数据库的 登录记录表 和用户表相关信息 相关信息
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            //客户端request中获取cookie
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals("ticket")) {
                    //判断请求request中是否存在有效的ticket
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        if (ticket != null) {
            loginTicketExample loginTicketExample = new loginTicketExample();
            com.zxc.zhihu.model.loginTicketExample.Criteria criteria = loginTicketExample.createCriteria();
            criteria.andTicketEqualTo(ticket);
            List<loginTicket> loginTickets = loginTicketMapper.selectByExample(loginTicketExample);
            if (loginTickets.size() < 1 || loginTickets.get(0).getExpired().before(new Date()) ||
                    loginTickets.get(0).getStatus() != 0) {
                //说明没有查找到了ticket,或者过期,或者已经登出
                return true;
            }

            //如果找到了，而且没有过期的话
            User user = userMapper.selectByPrimaryKey(loginTickets.get(0).getUserId());
            hostHolder.setUser(user);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    /**
     * 在线程销毁之前，将hostHolder的用户线程信息remove()移除
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @param e
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
