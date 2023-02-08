package Lim.boardApp.configuration;

import Lim.boardApp.ObjectValue.WhiteList;
import Lim.boardApp.interceptor.CustomerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomerInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(WhiteList.WHITELIST);
    }
}
