package Lim.boardApp.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "text_id")
    private Text text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="customer_id")
    private Customer customer;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Comment> childCommentList;

    public Comment(Text text, Customer customer, String content,Comment parent) {
        this.text = text;
        this.customer = customer;
        this.content = content;
        this.parent = parent;
    }

    public Comment(Text text, Customer customer, String content) {
        this.text = text;
        this.customer = customer;
        this.content = content;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customer.getCommentList().add(this);
    }

    public void setText(Text text){
        this.text= text;
        text.getCommentList().add(this);
    }

    public void setChildCommentList(Comment parent) {
        this.parent = parent;
        parent.getChildCommentList().add(this);
    }



}
