package Lim.boardApp.domain.text.entity;

import Lim.boardApp.domain.BaseEntity;

import lombok.Getter;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Text> textList = new ArrayList<>();

    public Board() {
    }

    public Board(String name) {
        this.name = name;
    }
}
