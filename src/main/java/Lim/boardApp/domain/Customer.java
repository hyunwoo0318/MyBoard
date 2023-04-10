package Lim.boardApp.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Customer  extends BaseEntity implements UserDetails, OAuth2User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_id")
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String name;

    private Integer age;

    private String role;

    @Column(unique = true)
    private Long kakaoId;

    @Column(unique = true)
    private String email;


    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Text> textList = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    public Customer(){}
    @Builder
    public Customer(String loginId, String password, String name, Integer age, String role, Long kakaoId,String email) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.age = age;
        this.role = role;
        this.kakaoId = kakaoId;
        this.email = email;
    }

    @Builder
    public Customer(String loginId, String password, String name, Integer age, String role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.age = age;
        this.role = role;
    }

    public void updateTextList(List<Text> textList){
        this.textList = textList;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    //for test
    public void setId(Long id){
        this.id = id;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(role));
        return authorityList;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
