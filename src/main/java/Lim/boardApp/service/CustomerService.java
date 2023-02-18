package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer findCustomer(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer findCustomer(String loginId){
        return customerRepository.findByLoginId(loginId).orElse(null);
    }

    public void addCustomer(CustomerRegisterForm form, int saltSize){
        String salt = makeSalt(saltSize);
        String passwordHash = hashPassword(form.getPassword(), salt);
        Customer customer = new Customer(form.getLoginId(), passwordHash + salt, form.getName(), form.getAge(), "USER",form.getKakaoId());
        customerRepository.save(customer);
    }
    public Customer login(String inputLoginId, String inputPassword){
        Optional<Customer> customerOptional = customerRepository.findByLoginId(inputLoginId);
        if(customerOptional.isEmpty()) {
            return null;
        }
        Customer customer = customerOptional.get();
        PasswordPair passwordPair = parsePasswordHash(customer.getPassword());
        String inputPasswordHash = hashPassword(inputPassword, passwordPair.salt);
        if(passwordPair.passwordHash.equals(inputPasswordHash)){
            return customer;
        }else{
            return null;
        }
    }

    //로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public boolean dupLoginId(CustomerRegisterForm customerRegisterForm){
        Optional<Customer> dup = customerRepository.findByLoginId(customerRegisterForm.getLoginId());
        if(dup.isEmpty()) return false;
        else return true;
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

    //카카오 로그인

    public Customer findKakao(Long kakaoId){
        Optional<Customer> customerOptional = customerRepository.findByKakaoId(kakaoId);
        return customerOptional.orElse(null);
    }

    public PasswordPair parsePasswordHash(String passwordHash){
        return new PasswordPair(passwordHash.substring(0, 64), passwordHash.substring(64));
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


