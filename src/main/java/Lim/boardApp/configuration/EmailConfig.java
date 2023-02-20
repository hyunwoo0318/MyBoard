package Lim.boardApp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.naver.com");
        javaMailSender.setPort(465);
        javaMailSender.setPassword("naverboard123");
        javaMailSender.setUsername("whskwock");
        javaMailSender.setJavaMailProperties(getProperties());
        javaMailSender.setDefaultEncoding("utf-8");
        return javaMailSender;
    }

    private Properties getProperties(){
        Properties pt = new Properties();

        pt.put("mail.smtp.starttls.enable", true);
        pt.put("mail.smtp.auth", true);
        pt.put("mail.smtp.timeout", 5000);
        pt.put("mail.smtp.ssl.enable", true);
        pt.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return pt;
    }
}
