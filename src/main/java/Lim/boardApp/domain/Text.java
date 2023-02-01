package Lim.boardApp.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
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

    public Text() {
    }

    @Builder
    public Text(String content, String title, Customer customer) {
        this.content = content;
        this.title = title;
        this.customer = customer;
    }

    public void setCustomer(Customer customer){
        this.customer = customer;
        customer.getTexts().add(this);
    }
}