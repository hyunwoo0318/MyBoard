package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final AuthenticationManagerBuilder authenticationManager;

    private final int saltSize = 20;
    public Customer findCustomer(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer findCustomer(String loginId){
        return customerRepository.findByLoginId(loginId).orElse(null);
    }

    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }


    public void addCustomer(CustomerRegisterForm form){
        String salt = makeSalt(saltSize);
        String passwordHash = hashPassword(form.getPassword(), salt);
        Customer customer = new Customer(form.getLoginId(), passwordHash + salt, form.getName(), form.getAge(), "USER",form.getKakaoId(),form.getEmail());
        customerRepository.save(customer);
    }

    public void changePassword(String password, Long id) {
        String salt  = makeSalt(saltSize);
        String passwordHash = hashPassword(password, salt);
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        optionalCustomer.get().changePassword(passwordHash + salt);
    }
    public Customer login(String inputLoginId, String inputPassword){
//        Optional<Customer> customerOptional = customerRepository.findByLoginId(inputLoginId);
//        if(customerOptional.isEmpty()) {
//            return null;
//        }
//        Customer customer = customerOptional.get();
//        PasswordPair passwordPair = parsePasswordHash(customer.getPassword());
//        String inputPasswordHash = hashPassword(inputPassword, passwordPair.salt);
//        if(passwordPair.passwordHash.equals(inputPasswordHash)){
//            return customer;
//        }else{
//            return null;
//        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(inputLoginId, inputPassword);
        Authentication authenticate = authenticationManager.getObject().authenticate(token);
        if (!authenticate.isAuthenticated()) {
            return null; // 로그인 실패
        }

        //로그인 성공
        Customer customer = customerRepository.findByLoginId(inputLoginId).get();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authenticate);

        return customer;
    }

    //로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public boolean dupLoginId(String loginId){
        Optional<Customer> dup = customerRepository.findByLoginId(loginId);
        if(dup.isEmpty()) return false;
        else return true;
    }

    public boolean dupEmail(CustomerRegisterForm customerRegisterForm) {
        Optional<Customer> dup = customerRepository.findByEmail(customerRegisterForm.getEmail());
        if (dup.isEmpty()) return false;
        else return true;
    }

    //카카오 로그인

    public Customer findKakao(Long kakaoId){
        Optional<Customer> customerOptional = customerRepository.findByKakaoId(kakaoId);
        return customerOptional.orElse(null);
    }

    //비밀번호 해시화
    public String makeSalt(int length){
        RandomString salt = new RandomString(length);
        return salt.nextString();
    }

    public String hashPassword(String password, String salt){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((password + salt).getBytes());
            byte[] digest = md.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02X", b));
            }
            return builder.toString();
        }catch(NoSuchAlgorithmException e){
            e.getMessage();
            return null;
        }
    }

    public PasswordPair parsePasswordHash(String passwordHash){
        return new PasswordPair(passwordHash.substring(0, 64), passwordHash.substring(64));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByLoginId(username).orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 회원입니다."));
        return User.builder()
                .username(customer.getUsername())
                .password(customer.getPassword())
                .authorities(customer.getAuthorities())
                .build();
    }

    private class PasswordPair {
        public String passwordHash;
        public String salt;

        private PasswordPair(String passwordHash, String salt){
            this.passwordHash= passwordHash;
            this.salt = salt;
        }
    }

}


