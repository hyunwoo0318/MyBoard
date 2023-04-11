package Lim.boardApp.controller;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.*;
import Lim.boardApp.form.*;
import Lim.boardApp.repository.*;
import Lim.boardApp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class TextController {

    private final TextService textService;
    private final CommentService commentService;
    private final TextHashtagService textHashtagService;
    private final HashtagService hashtagService;
    private final UploadFileService uploadFileService;

    /**
     * 게시글 리스트를 보여줌 -> 페이징 과정을 거침
     */
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

    /**
     * 키워드와 검색어의 종류를 입력받아 게시글 내에서 검색을 구현
     */
    @GetMapping("/search")
    public String searchText(@RequestParam(value = "searchKey") String searchKey,
                             @RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                             Model model) {

        String newSearchKey = "";
        PageForm pageForm = textService.pagingBySearch(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, searchKey, type);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", newSearchKey);
        return "board/textList";
    }

    /**
     * 특정 글의 내용,제목,작성자,댓글,대댓글을 보여줌
    */
    @GetMapping("show/{id}")
    public String showText(@PathVariable("id") Long id, @AuthenticationPrincipal Customer customer, Model model) throws NotFoundException {
        Long customerId = customer.getId();
        Text text = textService.findText(id);
        if(text == null){
            throw new NotFoundException();
        }
        boolean textOwn = false;
        if(customerId.equals(text.getCustomer().getId())) {
            textOwn = true;
        }
        List<Hashtag> hashtagList = textHashtagService.findHashtagList(text);
        List<Comment> commentList = commentService.findParentCommentList(text);
        model.addAttribute("text",text);
        model.addAttribute("hashtagList", hashtagList);
        model.addAttribute("commentList", commentList);
        model.addAttribute("owner", textOwn);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("textId", text.getId());

        return "board/showText";
    }

    /**
     * 게시글을 삭제, 수정, 생성함
     */
    @PostMapping("delete/{id}")
    public String deleteText(@PathVariable Long id) throws NotFoundException {
        Text text = textService.findText(id);
        if (text == null) {
            throw new NotFoundException();
        }
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
                              @AuthenticationPrincipal Customer customer) throws IOException {
        Long id = customer.getId();
        if (bindingResult.hasErrors()) {
            return "board/makeText";
        }
        List<Hashtag> hashtagList = new ArrayList<>();
        if(textCreateForm.getHashtags().length()!=0){
            hashtagList = hashtagService.parseHashtag(textCreateForm.getHashtags());
        }

        UploadFile uploadFile = uploadFileService.storeFile(textCreateForm.getFile());
        String fileName = null;
        if (uploadFile != null) {
            fileName = uploadFile.getStoredFileName();
        }
        if(textService.createText(id, textCreateForm,hashtagList,fileName) == null){
            System.out.println("create 오류");
            return "redirect:board/new";
        }
        return "redirect:/board";
    }

    @GetMapping("edit/{id}")
    public String getEditText(@PathVariable Long id, Model model) throws NotFoundException {
        Text text = textService.findText(id);
        if (text == null) {
            throw new NotFoundException();
        }
        String hashtags = hashtagService.mergeHashtag(textHashtagService.findHashtagList(text));
        TextUpdateForm textUpdateForm = new TextUpdateForm(text);
        textUpdateForm.setHashtags(hashtags);
        model.addAttribute("text", textUpdateForm);
        return "board/editText";
    }

    @PostMapping("edit/{id}")
    public String postEditText(@Validated @ModelAttribute("text") TextUpdateForm textUpdateForm, BindingResult bindingResult, @PathVariable Long id) throws NotFoundException {
        Text text = textService.findText(id);
        if (text == null) {
            throw new NotFoundException();
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/board/edit" + id;
        }
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textUpdateForm.getHashtags());
        if(textService.updateText(id, textUpdateForm,hashtagList) == null){
            System.out.println("update 실패");
        }
        return "redirect:/board/show/" + id;
    }

    /**
     * 새로운 댓글을 추가
     */
    @PostMapping("comments/new")
    public String postNewComment(@ModelAttribute("commentForm")CommentForm commentForm,
                                 @AuthenticationPrincipal Customer customer) throws NotFoundException {
        Text text = textService.findText(commentForm.getTextId());

        if (text == null || customer == null) {
            throw new NotFoundException();
        }else{
            Long textId = text.getId();
            commentService.addComment(text,customer, commentForm);
            return "redirect:/board/show/" + textId;
        }
    }

    /**
     * TODO : 1. 조회수 구현(단순히 들어올때 마다 오르기보다는 특정 로직을 이용해야함) -> Thread강의 듣고 구현
     * TODO : 2. 조회수 기반으로 게시글 정렬
     * TODO : 3. 글 상단 공지 고정(검색을 제외한 항상 맨 위에 위치하게함)
     * TODO : DTO구현해서 전체 변경하기.
     */
}
