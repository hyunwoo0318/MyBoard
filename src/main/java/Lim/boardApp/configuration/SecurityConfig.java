package Lim.boardApp.configuration;

import Lim.boardApp.service.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OauthService oauthService;
    private final RedisIndexedSessionRepository sessionRepository;

    private final String[] loginWhiteList = {"/static/**","/css/**","/*.ico","/error", "/","/customer-login/**", "/logout/**", "/register/**","/oauth/**","/kakao/**","/auth/**",
            "/swagger-ui/**","/js/**", "/api/**", "/find-password/**", "/new-password/**", "/#", "/news", "/img/**"};
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http.csrf().disable()
                .authorizeRequests()
                .antMatchers(loginWhiteList).permitAll()
                .anyRequest().authenticated().and()
//                .formLogin().loginPage("/customer-login").loginProcessingUrl("/customer-login").permitAll().and()
//                .logout().permitAll().and()
                .oauth2Login().loginPage("/customer-login")
                .failureHandler(customAuthenticationFailureHandler())
                .userInfoEndpoint().userService(oauthService)
                .and().and()
                .build();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            // 커스텀 로직을 구현하여 에러 처리를 수행합니다.
            response.sendRedirect("/customer-login?error=kakao_user_not_found");
        };
    }


}
