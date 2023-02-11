package Lim.boardApp.configuration;

import Lim.boardApp.ObjectValue.WhiteList;
import Lim.boardApp.interceptor.LoginInterceptor;
import Lim.boardApp.interceptor.TextAccessInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> loginWhiteList = Arrays.asList("/css/**","/*.ico","/error", "/", "/login", "/logout", "/register");
    private final List<String> textCheckList = Arrays.asList("/board/edit/**", "/board/delete/**");
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(loginWhiteList);

        registry.addInterceptor(textAccessInterceptor())
                .order(2)
                .addPathPatterns(textCheckList);
    }

    @Bean
    public TextAccessInterceptor textAccessInterceptor(){
        return new TextAccessInterceptor();
    }
}
