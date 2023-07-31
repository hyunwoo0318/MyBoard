package Lim.boardApp.domain;

import Lim.boardApp.ObjectValue.TextType;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
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

    @ColumnDefault("0L")
    private Long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Enumerated(EnumType.STRING)
    private TextType textType;

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL)
    private List<TextHashtag> textHashtagList = new ArrayList<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarkList = new ArrayList<>();

    public Text() {
    }

    @Builder
    public Text(String content, String title,String fileName ,Customer customer, Board  board, TextType textType) {
        this.content = content;
        this.title = title;
        this.customer = customer;
        this.fileName  = fileName;
        this.board = board;
        this.textType = textType;
    }

    @Builder
    public Text(String content, String title, Customer customer, Board  board,TextType textType) {
        this.content = content;
        this.title = title;
        this.customer = customer;
        this.board = board;
        this.textType = textType;
    }

    public void updateText(String content, String title){
        this.content = content;
        this.title = title;
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