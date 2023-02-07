package Lim.boardApp.controller;

import Lim.boardApp.PageConst;
import Lim.boardApp.SessionConst;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.TextHashtagRepository;
import Lim.boardApp.repository.TextRepository;
import Lim.boardApp.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class TextController {

    private final TextRepository textRepository;
    private final TextService textService;
    private final CustomerRepository customerRepository;
    private final TextHashtagRepository textHashtagRepository;

    //글 리스트 전체를 보여주는 페이지
    @GetMapping
    public String showTextList(@RequestParam(value = "page", defaultValue = "0") int page, Model model){

        String searchKey = new String();

        PageForm pageForm = textService.pagingByAll(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", searchKey);
        return "board/textList";
    }

    @GetMapping("/search/show")
    public String searchText( @RequestParam(value = "searchKey", defaultValue = "none", required = false) String searchKey,
                              @RequestParam(value = "type", defaultValue = "all",required = false) String type,
//                              @RequestParam(value = "page", defaultValue = "0",required = false) int page,
                              Model model){
        //TODO : input이 안받아지는 현상 -> post로 교체해서 해보기
        int page  =0;
        String newSearchKey = "";
        PageForm pageForm = textService.pagingBySearch(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, searchKey, type);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", newSearchKey);
        return "board/textList";
    }

    //선택한 글의 정보를 보여줌
    @GetMapping("/{id}")
    public String showText(@PathVariable("id") Long id, Model model) {
        Text text = textRepository.getReferenceById(id);
        List<Hashtag> hashtags = textHashtagRepository.findHashtagsByText(text);
        model.addAttribute(text);
        model.addAttribute(hashtags);
        return "board/showtext";
    }

    @PostMapping("/{id}")
    public String deleteText(@PathVariable Long id){
        textRepository.deleteById(id);
        return "redirect:/board";
    }

    //글 추가 메서드
    @GetMapping("/new")
    public String getNewText(Model model) {
        TextCreateForm textCreateForm = new TextCreateForm();
        model.addAttribute("text", textCreateForm);
        return "board/makeText";
    }

    @PostMapping("/new")
    public String postNewText(@ModelAttribute("text") TextCreateForm textCreateForm,
                              @SessionAttribute(name= SessionConst.LOGIN_CUSTOMER) Long id) {
        if(textService.createText(id, textCreateForm) == null){
            System.out.println("create 오류");
            return "redirect:/board/new";
        }
        return "redirect:/board";
    }

    //글 추가 메서드
    @GetMapping("/edit/{id}")
    public String getEditText(@PathVariable Long id, Model model) {
        Optional<Text> textOptional = textRepository.findById(id);
        //TODO : 예외처리
        Text text = textOptional.get();
        TextUpdateForm textUpdateForm = new TextUpdateForm(text);
        model.addAttribute("text", textUpdateForm);
        return "board/editText";
    }

    @PostMapping("/edit/{id}")
    public String postEditText(@ModelAttribute("text") TextUpdateForm textUpdateForm,@PathVariable Long id) {
        if(textService.updateText(id, textUpdateForm) == null){
            System.out.println("update 실패");
        }
        return "redirect:/board";
    }
}
