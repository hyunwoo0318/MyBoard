package Lim.boardApp.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Text extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="text_id")
    private Long id;

    @Lob
    private String content;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    private String fileName;

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL)
    private List<TextHashtag> textHashtagList = new ArrayList<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarkList = new ArrayList<>();

    public Text() {

    }

    @Builder
    public Text(String content, String title,String fileName ,Customer customer) {
        this.content = content;
        this.title = title;
        this.customer = customer;
        this.fileName  = fileName;
    }

    public Text(String content, String title, Customer customer) {
        this.content = content;
        this.title = title;
        this.customer = customer;
    }

    public void updateText(String content, String title, String fileName){
        this.content = content;
        this.title = title;
        this.fileName = fileName;
    }

    public void setCustomer(Customer customer){
        this.customer = customer;
        customer.getTextList().add(this);
    }

    //for test
    public void setId(Long id){
        this.id = id;
    }
}