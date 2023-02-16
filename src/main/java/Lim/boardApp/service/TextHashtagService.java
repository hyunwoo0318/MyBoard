package Lim.boardApp.service;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.TextHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TextHashtagService {

    private final TextHashtagRepository textHashtagRepository;

    public List<Hashtag> findHashtagList(Text text) {
        return textHashtagRepository.findHashtagsByText(text);
    }

}
