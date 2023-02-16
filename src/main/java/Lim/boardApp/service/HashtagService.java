package Lim.boardApp.service;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    /**
     * ,를 구분자로 받은 hashtag를 파싱해서 새로운 해시태그면 저장하는 방식
     * @param ','를 구분자로 가지는 hashtag모음
     * @return List<Hashtag>
     */
    public List<Hashtag> parseHashtag(String hashtags){
        String[] tagList = hashtags.replaceAll(" ", "").split(",");
        List<Hashtag> hashtagList = new ArrayList<>();
        for (String t : tagList) {
            t = "#" + t;
            Hashtag tag = hashtagRepository.findByName(t);
            if(tag == null){
                //새로운 hashtag
                Hashtag newTag = new Hashtag(t);
                hashtagRepository.save(newTag);
                hashtagList.add(newTag);
            }else{
                hashtagList.add(tag);
            }
        }
        return hashtagList;
    }

    /**
     * hashtagList를 다시 사용자가 수정할수 있게 원래의 형식으로 돌려놓음
     * @param hashtagList
     * @return String(','로 구분된 hashtag집합)
     */
    public String mergeHashtag(List<Hashtag> hashtagList) {
        return hashtagList.stream().map(h -> h.getName().replaceAll("#","")).collect(Collectors.joining(","));
    }

    public Hashtag addHashtag(String name) {
        Hashtag hashtag = new Hashtag(name);
        return hashtagRepository.save(hashtag);
    }
}
