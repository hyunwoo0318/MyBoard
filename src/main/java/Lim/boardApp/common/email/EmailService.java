package Lim.boardApp.common.email;

import static Lim.boardApp.common.exception.ExceptionInfo.EMAIL_AUTH_FAIL;
import static Lim.boardApp.common.exception.ExceptionInfo.NOT_FOUND;

import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import net.jodah.expiringmap.ExpiringMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    // 이메일 인증번호를 저장해놓는 맵
    private final ExpiringMap<String, String> emailMap;
    private final CustomerRepository customerRepository;

    public boolean checkEmailForm(String email) {
        if (email == null) return false;
        String regex = "^(.+)@(\\S+)$";
        return Pattern.compile(regex).matcher(email).matches();
    }


    public void sendEmailAuth(String email) throws MessagingException {

        Random rand = new Random();
        String code = String.valueOf(rand.nextInt(10000));
        Address toMail = new InternetAddress(email);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessage.setText("인증 메일 입니다. 인증번호 = " + code, "utf-8");
        mimeMessage.setSubject("인증메일 - 제목");
        mimeMessage.setFrom("whskwock@naver.com");
        mimeMessage.addRecipient(MimeMessage.RecipientType.TO, toMail);

        emailMap.put(email, code);

        mailSender.send(mimeMessage);
    }

    public Long checkEmailAuth(String email, String emailAuth) {
        String code = emailMap.get(email);
        if (!code.equals(emailAuth)) {
            throw new CustomException(EMAIL_AUTH_FAIL);
        }

        // 이메일 인증이 성공한 경우 해당 이메일을 가진 회원의 id 리턴
        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    throw new CustomException(NOT_FOUND);
                                });

        return customer.getId();
    }
}
