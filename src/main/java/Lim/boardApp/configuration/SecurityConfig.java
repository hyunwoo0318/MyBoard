package Lim.boardApp.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] loginWhiteList = {"/css/**","/*.ico","/error", "/","/customer-login", "/logout", "/register","/oauth/**","/kakao/**","/auth/**",
            "/swagger-ui/**", "/api/**", "/find-password/**", "/new-password/**"};
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
                .formLogin().loginPage("/customer-login").loginProcessingUrl("/customer-login").permitAll().disable()
                .logout().permitAll().and()
                .build();
    }
}
