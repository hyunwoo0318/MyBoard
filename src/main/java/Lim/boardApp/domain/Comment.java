package Lim.boardApp.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

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

    @Column(name = "parent_comment_id")
    private Long parentCommentId=-1L;

    public Comment(Text text, Customer customer, String content) {
        this.text = text;
        this.customer = customer;
        this.content = content;
        this.parentCommentId = this.id;
    }

    public Comment(Text text, Customer customer, String content, Long parentCommentId) {
        this.text = text;
        this.customer = customer;
        this.content = content;
        this.parentCommentId =parentCommentId;
    }
}
