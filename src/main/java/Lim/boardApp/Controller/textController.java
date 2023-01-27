package Lim.boardApp.Controller;

import Lim.boardApp.PageConst;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.repository.TextRepository;
import Lim.boardApp.service.PagingService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/board")
public class textController {

    //TODO : 페이징 작업 -> prev, next에서 오류가 발생함.
    private final TextRepository textRepository;
    private final PagingService pagingService;
    public textController(TextRepository textRepository, PagingService pagingService){
        this.textRepository = textRepository;
        this.pagingService = pagingService;
    }

    //글 리스트 전체를 보여주는 페이지
    @GetMapping
    public String showTextList(Model model, @RequestParam(value = "page", defaultValue = "1") int page){

        PageRequest size = PageRequest.ofSize(PageConst.PAGE_BLOCK_SIZE);
        Page<Text> tp = textRepository.findAll(size);
        int totalPages = tp.getTotalPages();

        if(page -1 >= totalPages) {
            return "redirect:/board?page=" + Integer.toString(totalPages);
        }
        if(page < 1) {
            return "redirect:/board/?page=1";
        }

        PageRequest pageRequest = PageRequest.of(page-1, PageConst.PAGE_BLOCK_SIZE);
        Page<Text> textList = textRepository.findAll(pageRequest);
        PageBlockForm block = pagingService.findBlock(page-1, totalPages);

        model.addAttribute("block", block);
        model.addAttribute("textList", textList);
        model.addAttribute("curPage", page);

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
    //TODO: update전용 객체를 만들어서 받고 넘기기.(제목하고 내용만 바꾸면됨.)
    @GetMapping("/new")
    public String goToMakeText(@RequestParam(value = "tid",required = false,defaultValue = "") Long tid ,Model model) {
        Text text = new Text();
        text.setTid(tid);
        model.addAttribute("text", text);
        return "board/maketext";
    }

    @PostMapping("/new")
    public String makeText(@ModelAttribute Text text) {
        textRepository.save(text);
        return "redirect:/board";
    }
}
