package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;

@Configuration
public class leyouCorsConfig {
    // 跨域允许访问

    @Bean
    public CorsFilter corsFilter(){
//        cors配置信息
        CorsConfiguration config = new CorsConfiguration();
//        允许的域
//        后台管理
        config.addAllowedOrigin("http://manage.leyou.com");
//        前台门户
        config.addAllowedOrigin("http://www.leyou.com");
//        是否发送cookie
        config.setAllowCredentials(true);
//        允许的请求方式
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
//        允许的头信息
        config.addAllowedHeader("*");
//        添加映射路径
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",config);
        return new CorsFilter(configSource);
    }
}
