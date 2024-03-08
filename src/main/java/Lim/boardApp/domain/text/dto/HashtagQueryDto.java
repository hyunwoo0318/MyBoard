package Lim.boardApp.domain.text.dto;

import Lim.boardApp.domain.text.entity.Hashtag;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HashtagQueryDto {
    private Long id;
    private String name;

    public HashtagQueryDto(Hashtag hashtag){
        this.id = hashtag.getId();
        this.name = hashtag.getName();
    }
}
