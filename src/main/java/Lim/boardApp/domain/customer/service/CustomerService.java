package Lim.boardApp.domain.customer.service;

import static Lim.boardApp.common.exception.ExceptionInfo.DUP_EMAIL;
import static Lim.boardApp.common.exception.ExceptionInfo.DUP_LOGIN_ID;
import static Lim.boardApp.common.exception.ExceptionInfo.INVALID_PASSWORD_CHECK;
import static Lim.boardApp.common.exception.ExceptionInfo.LOGIN_FAIL;
import static Lim.boardApp.common.exception.ExceptionInfo.NOT_FOUND;

import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.domain.bookmark.entity.Bookmark;
import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.customer.dto.CustomerProfileDto;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.customer.form.CustomerRegisterForm;
import Lim.boardApp.domain.customer.form.LoginForm;
import Lim.boardApp.domain.customer.form.PasswordChangeForm;
import Lim.boardApp.domain.customer.repository.CustomerRepository;
import Lim.boardApp.domain.text.entity.Text;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class CustomerService implements UserDetailsService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void addCustomer(CustomerRegisterForm form) {

        // 로그인아이디 중복 체크
        if (dupLoginId(form.getLoginId())) throw new CustomException(DUP_LOGIN_ID);

        // 이메일 중복 체크
        if (dupEmail(form.getEmail())) throw new CustomException(DUP_EMAIL);

        // 비밀번호와 비밀번호 확인이 같은지 체크
        if (!form.getPassword().equals(form.getPasswordCheck()))
            throw new CustomException(INVALID_PASSWORD_CHECK);

        Customer customer =
                Customer.builder()
                        .age(form.getAge())
                        .loginId(form.getLoginId())
                        .password(passwordEncoder.encode(form.getPassword()))
                        .email(form.getEmail())
                        .kakaoId(form.getKakaoId())
                        .name(form.getName())
                        .role("USER")
                        .build();

        customerRepository.save(customer);
    }

    public void changePassword(PasswordChangeForm form, Long id) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    throw new CustomException(NOT_FOUND);
                                });

        if (!form.getPassword().equals(form.getPasswordCheck())) {
            throw new CustomException(INVALID_PASSWORD_CHECK);
        }

        customer.changePassword(passwordEncoder.encode(form.getPassword()));
    }

    @Transactional
    public void login(LoginForm form) {

        String inputLoginId = form.getLoginId();

        // 로그인 성공
        customerRepository
                .findByLoginId(inputLoginId)
                .ifPresentOrElse(
                        (findCustomer) -> {
                            UsernamePasswordAuthenticationToken token =
                                    new UsernamePasswordAuthenticationToken(
                                            form.getLoginId(), form.getPassword());
                            AuthenticationManager object = authenticationManagerBuilder.getObject();
                            Authentication authentication = object.authenticate(token);

                            SecurityContext securityContext = SecurityContextHolder.getContext();
                            securityContext.setAuthentication(authentication);
                        },
                        () -> {
                            throw new CustomException(LOGIN_FAIL);
                        });
    }

    // 로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public boolean dupLoginId(String loginId) {
        Optional<Customer> dup = customerRepository.findByLoginId(loginId);
        return dup.isPresent();
    }

    public boolean dupEmail(String email) {
        Optional<Customer> dup = customerRepository.findByEmail(email);
        return dup.isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer =
                customerRepository
                        .findByLoginId(username)
                        .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 회원입니다."));
        return customer;
    }

    @Transactional(readOnly = true)
    public CustomerProfileDto getCustomerProfile(String loginId) {
        Customer customer =
                customerRepository
                        .findByLoginId(loginId)
                        .orElseThrow(() -> new CustomException(NOT_FOUND));

        List<Text> textList = customer.getTextList();
        List<Comment> commentList = customer.getCommentList();
        List<Text> bookmarkedTextList =
                customer.getBookmarkList().stream()
                        .map(Bookmark::getText)
                        .collect(Collectors.toList());

        return new CustomerProfileDto(
                customer.getLoginId(),
                customer.getEmail(),
                customer.getName(),
                customer.getAge(),
                textList,
                commentList,
                bookmarkedTextList);
    }
}
