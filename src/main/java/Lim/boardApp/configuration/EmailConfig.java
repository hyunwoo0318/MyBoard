package Lim.boardApp.configuration;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
public class EmailConfig{

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private boolean tlsEnable = true;
    private boolean auth = true;
    private boolean sslEnable = true;
    private String socketClass = "javax.net.ssl.SSLSocketFactory";
    private int timeout = 5000;

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setPassword(password);
        javaMailSender.setUsername(username);
        javaMailSender.setJavaMailProperties(getProperties());
        javaMailSender.setDefaultEncoding("utf-8");
        return javaMailSender;
    }

    @Bean
    public ExpiringMap<String, String> expiringMap(){
        ExpiringMap<String,String> map = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiration(10 , TimeUnit.MINUTES)
                .build();
        return map;
    }

    private Properties getProperties(){
        Properties pt = new Properties();

        pt.put("mail.smtp.starttls.enable", tlsEnable);
        pt.put("mail.smtp.auth", auth);
        pt.put("mail.smtp.timeout", timeout);
        pt.put("mail.smtp.ssl.enable", sslEnable);
        pt.put("mail.smtp.socketFactory.class", socketClass);
        return pt;
    }
}
