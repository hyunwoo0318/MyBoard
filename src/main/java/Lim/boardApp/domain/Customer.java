package Lim.boardApp.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/* customer 테이블
    cid varchar pk,
    loginId varchar unique
    password varchar,
    cname varchar,
    age int
* */
@Entity
@Getter
@Setter
public class Customer extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_id")
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String cname;

    private Integer age;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    List<Text> texts = new ArrayList<>();

    public Customer(){}
    @Builder
    public Customer(String loginId, String password, String cname, Integer age) {
        this.loginId = loginId;
        this.password = password;
        this.cname = cname;
        this.age = age;
    }
}
