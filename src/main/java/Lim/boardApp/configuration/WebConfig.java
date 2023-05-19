package Lim.boardApp.configuration;

import Lim.boardApp.interceptor.BoardTypesInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> loginWhiteList = Arrays.asList("/css/**","/*.ico","/error", "/", "/login", "/logout", "/register","/oauth/**","/kakao/**","/auth/**",
            "/swagger-ui/**", "/api/**", "/find-password/**", "/new-password/**");
    private final List<String> textCheckList = Arrays.asList("/board/edit/**", "/board/delete/**");
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(boardTypesInterceptor())
                .order(1)
                .addPathPatterns("/**");
    }

    @Bean
    public BoardTypesInterceptor boardTypesInterceptor(){
        return new BoardTypesInterceptor();
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
