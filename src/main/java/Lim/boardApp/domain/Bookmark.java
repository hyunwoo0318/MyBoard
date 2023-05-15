package Lim.boardApp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Bookmark extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="text_id")
    private Text text;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Builder
    public Bookmark(Text text, Customer customer) {
        this.text = text;
        this.customer = customer;
    }


}
