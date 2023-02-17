package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.*;
import Lim.boardApp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class TextController {

    private final TextService textService;
    private final CommentService commentService;
    private final CustomerService customerService;
    private final TextHashtagService textHashtagService;
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
    public String showText(@PathVariable("id") Long id, @SessionAttribute(SessionConst.LOGIN_CUSTOMER) Long customerId, Model model) {
        Text text = textService.findText(id);
        if(text == null){
            return "board/textList";
        }
        List<Hashtag> hashtagList = textHashtagService.findHashtagList(text);
        List<Comment> commentList = commentService.findCommentList(text);
        model.addAttribute("text",text);
        model.addAttribute("hashtagList", hashtagList);
        model.addAttribute("commentList", commentList);
        if(customerId == text.getCustomer().getId()){
            return "board/showMyText";
        }else{
            return "board/showtext";
        }

    }

    @PostMapping("delete/{id}")
    public String deleteText(@PathVariable Long id){
        textService.deleteText(id);
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
    public String postNewText(@Validated @ModelAttribute("text") TextCreateForm textCreateForm, BindingResult bindingResult,
                              @SessionAttribute(name= SessionConst.LOGIN_CUSTOMER) Long id) {
        if (bindingResult.hasErrors()) {
            return "board/makeText";
        }
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textCreateForm.getHashtags());
        if(textService.createText(id, textCreateForm,hashtagList) == null){
            System.out.println("create 오류");
            return "redirect:/board/new";
        }
        return "redirect:/board";
    }

    @GetMapping("edit/{id}")
    public String getEditText(@PathVariable Long id, Model model) {
        Text text = textService.findText(id);
        if (text == null) {
            return "/board/textList";
        }
        String hashtags = hashtagService.mergeHashtag(textHashtagService.findHashtagList(text));
        TextUpdateForm textUpdateForm = new TextUpdateForm(text);
        textUpdateForm.setHashtags(hashtags);
        model.addAttribute("text", textUpdateForm);
        return "board/editText";
    }

    @PostMapping("edit/{id}")
    public String postEditText(@Validated @ModelAttribute("text") TextUpdateForm textUpdateForm, BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            return "redirect:/board/edit" + id;
        }
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textUpdateForm.getHashtags());
        if(textService.updateText(id, textUpdateForm,hashtagList) == null){
            System.out.println("update 실패");
        }
        return "redirect:/board/show/" + id;
    }

    //새 댓글 추가
    @GetMapping("/comment/new/{id}")
    public String getNewComment(@PathVariable Long id,Model model){
        Text text = textService.findText(id);
        if(text == null){
            return "board/textList";
        }
        List<Comment> commentList = commentService.findCommentList(text);
        String commentContent = "";
        model.addAttribute("text", text);
        model.addAttribute("commentList", commentList);
        model.addAttribute("commentContent", commentContent);
        return "board/newComment";
    }

    @PostMapping("comment/new/{id}")
    public String postNewComment(@PathVariable Long id,@ModelAttribute("commentContent") String commentContent,
                                 @SessionAttribute(SessionConst.LOGIN_CUSTOMER) Long customerId) {
        Text text = textService.findText(id);
        if(text == null){
            return "board/textList";
        }
        Customer customer = customerService.findCustomer(customerId);
        if(customer != null){
            commentService.addComment(text, customer, commentContent);
        }
        return "redirect:/board/show/" + id;
    }
}
