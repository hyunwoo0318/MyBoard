package Lim.boardApp.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter
public class Customer  extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_id")
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String name;

    private Integer age;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Text> texts = new ArrayList<>();


    public Customer(){}
    @Builder
    public Customer(String loginId, String password, String name, Integer age) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.age = age;
    }


}
