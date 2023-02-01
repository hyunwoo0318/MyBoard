package Lim.boardApp.Controller;

import Lim.boardApp.PageConst;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.repository.TextRepository;
import Lim.boardApp.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class textController {

    private final TextRepository textRepository;
    private final TextService textService;

    //글 리스트 전체를 보여주는 페이지
    @GetMapping
    public String showTextList(Model model, @RequestParam(value = "page", defaultValue = "0") int page){

        int lastPage = textService.getLastPage(PageConst.PAGE_SIZE);

        if(page < 0) page = 0;
        if(lastPage < page) page = lastPage;

        PageRequest pageRequest = PageRequest.of(page, PageConst.PAGE_SIZE);
        Page<Text> pages = textRepository.findAll(pageRequest);
        List<Text> textList = pages.getContent();

        PageBlockForm block = textService.findBlock(page,lastPage, PageConst.PAGE_BLOCK_SIZE);

        model.addAttribute("textList", textList);
        model.addAttribute("page", page);
        model.addAttribute("block", block);

        return "board/textlist";
    }

    //선택한 글의 정보를 보여줌
    @GetMapping("/{tid}")
    public String showText(@PathVariable("tid") Long tid, Model model) {
        Text text = textRepository.getReferenceById(tid);
        model.addAttribute(text);
        return "board/showtext";
    }

    @PostMapping("/{tid}")
    public String deleteText(@PathVariable Long tid){
        textRepository.deleteById(tid);
        return "redirect:/board";
    }

    //글을 추가 및 수정하는 메서드
    @GetMapping("/new")
    public String goToMakeText(@RequestParam(value = "id",required = false,defaultValue = "-1") Long id ,Model model) {

        Optional<Text> findText = textRepository.findById(id);
        Text text = findText.orElse(new Text());

        model.addAttribute("text", text);
        return "board/maketext";
    }

    @PostMapping("/new")
    public String makeText(@ModelAttribute Text text) {
        textRepository.save(text);
        return "redirect:/board";
    }
}
