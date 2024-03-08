package Lim.boardApp.domain.bookmark.entity;

import Lim.boardApp.domain.BaseEntity;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.entity.Text;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Bookmark extends BaseEntity {

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
