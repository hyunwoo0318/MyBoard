package Lim.boardApp.configuration;

import Lim.boardApp.constant.RoleConst;
import Lim.boardApp.constant.TextType;
import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class InitConfig {

    private final CustomerRepository customerRepository;
    private final BoardRepository boardRepository;
    private final TextRepository textRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void intitData(){
        Customer customer = initCustomer();
        initText(customer);
    }
    private Customer initCustomer(){
        Customer testCustomer = Customer.builder()
            .age(123)
            .email("hyunwoo0318@naver.com")
            .role(RoleConst.USER)
            .loginId("test")
            .password(passwordEncoder.encode("test"))
            .name("임현우")
            .build();

        customerRepository.save(testCustomer);
        return testCustomer;
    }

    public void initText(Customer customer){
        Board board = new Board("test");
        boardRepository.save(board);

        for(int i=0;i<1000;i++){
            Text text = Text.builder()
                .board(board)
                .textType(TextType.GENERAL)
                .title("title" + i)
                .content("content" + i)
                .customer(customer)
                .build();
            textRepository.save(text);
        }
    }


}

