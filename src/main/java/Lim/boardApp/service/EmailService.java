package Lim.boardApp.service;

import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    //이메일 인증번호를 저장해놓는 맵
    private Map<String, String> emailMap = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(10 , TimeUnit.MINUTES)
            .build();


    public boolean checkEmailForm(String email){
        if(email==null) return false;
        String regex = "^(.+)@(\\S+)$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    public boolean findPrevAuth(String email){
        return emailMap.containsKey(email);
    }


    public void sendEmailAuth(String email) throws MessagingException {
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 8);
        Address toMail = new InternetAddress(email);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessage.setText("인증 메일 입니다. 인증번호 = " + code, "utf-8");
        mimeMessage.setSubject("인증메일 - 제목");
        mimeMessage.setFrom("whskwock@naver.com");
        mimeMessage.addRecipient(MimeMessage.RecipientType.TO, toMail);

        emailMap.put(email, code);

        mailSender.send(mimeMessage);
    }

    public Boolean checkEmailAuth(String email, String emailAuth) {
        String code = emailMap.get(email);
        return code.equals(emailAuth);
    }

}
