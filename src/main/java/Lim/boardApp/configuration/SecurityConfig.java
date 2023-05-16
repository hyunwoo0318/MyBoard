package Lim.boardApp.configuration;

import Lim.boardApp.service.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OauthService oauthService;
    private final RedisIndexedSessionRepository sessionRepository;

    private final String[] loginWhiteList = {"/static/**","/css/**","/*.ico","/error", "/","/customer-login/**", "/logout/**", "/register/**","/oauth/**","/kakao/**","/auth/**",
            "/swagger-ui/**","/js/**", "/api/**", "/find-password/**", "/new-password/**", "/#"};
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
                .userInfoEndpoint().userService(oauthService)
                .and().and()
                .build();
    }

}
