package com.zxc.zhihu.configuration;

import com.zxc.zhihu.interceptor.LoginIntecepter;
import com.zxc.zhihu.interceptor.PassportIntecepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 拦截器配置文件
 *
 * @date 2021
 */
@Component
public class ZhihuWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportIntecepter passportIntecepter;

    @Autowired
    LoginIntecepter loginIntecepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportIntecepter);
        registry.addInterceptor(loginIntecepter).addPathPatterns("/user/*");
        super.addInterceptors(registry);
    }
}
