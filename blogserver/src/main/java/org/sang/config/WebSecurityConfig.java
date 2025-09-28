package org.sang.config;

import org.sang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.sang.bean.User;
import org.sang.service.LoginLogService;

/**
 * Created by sang on 2017/12/17.
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserService userService;

    @Autowired
    LoginLogService loginLogService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/category/all").authenticated()
                .antMatchers("/article/public/**").permitAll()
                .antMatchers("/admin/**","/reg").hasRole("超级管理员")
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login_page").successHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
                // 获取用户信息
                String username = authentication.getName();
                String ipAddress = getClientIpAddress(httpServletRequest);
                
                try {
                    // 通过用户名查询用户详细信息
                    User user = (User) userService.loadUserByUsername(username);
                    Long userId = null;
                    String nickname = username;
                    
                    if (user != null && user.getId() != null) {
                        userId = user.getId();
                        nickname = user.getNickname() != null ? user.getNickname() : username;
                    }
                    
                    // 记录登录成功日志
                    loginLogService.recordLoginLog(
                        userId, 
                        username, 
                        nickname, 
                        ipAddress, 
                        "SUCCESS"
                    );
                    System.out.println("登录日志记录成功: " + username + ", userId: " + userId);
                } catch (Exception e) {
                    System.err.println("记录登录日志失败: " + e.getMessage());
                    e.printStackTrace();
                }
                
                httpServletResponse.setContentType("application/json;charset=utf-8");
                PrintWriter out = httpServletResponse.getWriter();
                out.write("{\"status\":\"success\",\"msg\":\"登录成功\"}");
                out.flush();
                out.close();
            }
        })
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String username = httpServletRequest.getParameter("username");
                        String ipAddress = getClientIpAddress(httpServletRequest);
                        
                        // 记录登录失败日志
                        loginLogService.recordLoginFailure(username, ipAddress, e.getMessage());
                        
                        httpServletResponse.setContentType("application/json;charset=utf-8");
                        PrintWriter out = httpServletResponse.getWriter();
                        out.write("{\"status\":\"error\",\"msg\":\"登录失败\"}");
                        out.flush();
                        out.close();
                    }
                }).loginProcessingUrl("/login")
                .usernameParameter("username").passwordParameter("password").permitAll()
                .and().logout().permitAll().and().csrf().disable().exceptionHandling().accessDeniedHandler(getAccessDeniedHandler());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/blogimg/**","/index.html","/static/**");
    }

    @Bean
    AccessDeniedHandler getAccessDeniedHandler() {
        return new AuthenticationAccessDeniedHandler();
    }

    // 添加获取客户端IP的方法
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}