package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.HashtagRepository;
import Lim.boardApp.repository.TextHashtagRepository;
import Lim.boardApp.repository.TextRepository;
import Lim.boardApp.service.HashtagService;
import Lim.boardApp.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class TextController {
    private final HashtagRepository hashtagRepository;

    private final TextRepository textRepository;
    private final TextService textService;
    private final TextHashtagRepository textHashtagRepository;
    private final HashtagService hashtagService;

    //글 리스트 전체를 보여주는 페이지
    @GetMapping
    public String showTextList(@RequestParam(value = "page", defaultValue = "0") int page, Model model){

        String searchKey="";
        String type="";

        PageForm pageForm = textService.pagingByAll(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", searchKey);
        model.addAttribute("type", type);
        return "board/textList";
    }


    @GetMapping("/search")
    public String searchText( @RequestParam(value = "searchKey") String searchKey,
                              @RequestParam(value = "type",required = false) String type,
                              @RequestParam(value = "page", defaultValue = "0",required = false) int page,
                              Model model){
        String newSearchKey = "";
        PageForm pageForm = textService.pagingBySearch(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, searchKey, type);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", newSearchKey);
        return "board/textList";
    }

    //선택한 글의 정보를 보여줌
    @GetMapping("show/{id}")
    public String showText(@PathVariable("id") Long id, Model model) {
        Text text = textRepository.getReferenceById(id);
        List<Hashtag> hashtags = textHashtagRepository.findHashtagsByText(text);
        model.addAttribute(text);
        model.addAttribute(hashtags);
        return "board/showtext";
    }

    @PostMapping("show/{id}")
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
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textCreateForm.getHashtags());
        if(textService.createText(id, textCreateForm,hashtagList) == null){
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
        String hashtags = hashtagService.mergeHashtag(textHashtagRepository.findHashtagsByText(text));
        TextUpdateForm textUpdateForm = new TextUpdateForm(text);
        textUpdateForm.setHashtags(hashtags);
        model.addAttribute("text", textUpdateForm);
        return "board/editText";
    }

    @PostMapping("/edit/{id}")
    public String postEditText(@ModelAttribute("text") TextUpdateForm textUpdateForm,@PathVariable Long id) {
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textUpdateForm.getHashtags());
        if(textService.updateText(id, textUpdateForm,hashtagList) == null){
            System.out.println("update 실패");
        }
        return "redirect:/board";
    }
}