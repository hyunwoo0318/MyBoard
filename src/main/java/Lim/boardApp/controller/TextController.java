package Lim.boardApp.controller;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.domain.*;
import Lim.boardApp.form.*;
import Lim.boardApp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TextController {

    private final TextService textService;
    private final CommentService commentService;
    private final TextHashtagService textHashtagService;
    private final HashtagService hashtagService;
    private final UploadFileService uploadFileService;
    private final BookmarkService bookmarkService;

    /**
     * 게시글 리스트를 보여줌 -> 페이징 과정을 거침
     */
    @GetMapping("/")
    public String showTextList(@RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "board-name", defaultValue = "전체") String boardName,
                               Model model){
        String searchKey="";
        String type="";

        PageForm pageForm = textService.pagingByAll(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, boardName);
        model.addAttribute("boardName", boardName);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", searchKey);
        model.addAttribute("type", type);
        return "board/textList";
    }

    //TODO
    /**
     * 키워드와 검색어의 종류를 입력받아 게시글 내에서 검색을 구현
     */
    @GetMapping("/search")
    public String searchText(@RequestParam(value = "searchKey") String searchKey,
                             @RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                             @RequestParam(value = "board-name", defaultValue = "전체", required = false)String boardName,
                             Model model) {

        String newSearchKey = "";
        PageForm pageForm = textService.pagingBySearch(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, searchKey, type,boardName);
        model.addAttribute("boardName", boardName);
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
        //글의 주인인지 확인
        boolean textOwn = false;
        if(customerId.equals(text.getCustomer().getId())) {
            textOwn = true;
        }

        if (!textOwn) {
            textService.increaseViewCnt(text, customerId);
        }

        //글을 조회하는 사람이 해당 글을 북마크했는지 확인
        boolean isBookmarked = false;
        List<Bookmark> bookmarkList = text.getBookmarkList();
        for (Bookmark bookmark : bookmarkList) {
            Long bookmarkCustomerId = bookmark.getCustomer().getId();
            if (bookmarkCustomerId.equals(customerId)) {
                isBookmarked = true;
                break;
            }
        }


        List<Hashtag> hashtagList = textHashtagService.findHashtagList(text);
        List<Comment> commentList = commentService.findParentCommentList(text);
        int commentCnt = commentService.findCommentCnt(text.getId());


        model.addAttribute("text",text);
        model.addAttribute("hashtagList", hashtagList);
        model.addAttribute("commentList", commentList);
        model.addAttribute("commentCnt", commentCnt);
        model.addAttribute("owner", textOwn);
        model.addAttribute("isBookmarked", isBookmarked);

        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("customerId", customerId);
        model.addAttribute("textId", text.getId());

        return "board/showText";
    }

    /**
     * 게시글을 삭제, 수정, 생성함
     */
    @PostMapping("delete/{id}")
    public String deleteText(@PathVariable Long id) throws NotFoundException {
        textService.findText(id);
        textService.deleteText(id);
        return "redirect:/";
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

      /*  UploadFile uploadFile = uploadFileService.storeFile(textCreateForm.getFile());
        String fileName = null;
        if (uploadFile != null) {
            fileName = uploadFile.getStoredFileName();
        }*/
        textService.createText(id, textCreateForm,hashtagList,null);

        return "redirect:/";
    }

    @GetMapping("edit/{id}")
    public String getEditText(@PathVariable Long id, Model model, @AuthenticationPrincipal Customer customer, HttpServletResponse response) throws NotFoundException, IOException {
        Text text = textService.findText(id);
        Long ownerId = text.getCustomer().getId();
        if (!ownerId.equals(customer.getId())) {
            response.sendError(403);
        }
        String hashtags = hashtagService.mergeHashtag(textHashtagService.findHashtagList(text));
        TextUpdateForm textUpdateForm = new TextUpdateForm(text);
        textUpdateForm.setHashtags(hashtags);
        model.addAttribute("text", textUpdateForm);
        return "board/makeText";
    }

    @PostMapping("edit/{id}")
    public String postEditText(@Validated @ModelAttribute("text") TextUpdateForm textUpdateForm, BindingResult bindingResult, @PathVariable Long id) throws NotFoundException {
        Text text = textService.findText(id);
        if (bindingResult.hasErrors()) {
            return "redirect:/edit" + id;
        }
        List<Hashtag> hashtagList = hashtagService.parseHashtag(textUpdateForm.getHashtags());
        if(textService.updateText(id, textUpdateForm,hashtagList) == null){
            System.out.println("update 실패");
        }
        return "redirect:/show/" + id;
    }

    /**
     * 새로운 댓글을 추가
     */
    @PostMapping("comments/new")
    public String postNewComment(@ModelAttribute("commentForm")CommentForm commentForm,
                                 @AuthenticationPrincipal Customer customer) throws NotFoundException {
        Text text = textService.findText(commentForm.getTextId());


            Long textId = text.getId();
            commentService.addComment(text,customer, commentForm);
            return "redirect:/show/" + textId;

    }

    /**
     * 북마크 실행, 취소
     */

    @PostMapping("/bookmarks/new")
    public String postNewBookmark(@RequestParam("textId") Long textId, @AuthenticationPrincipal Customer customer) throws NotFoundException{
        Text text = textService.findText(textId);
        bookmarkService.addBookmark(text, customer);
        return "redirect:/show/" + textId;

    }

    @PostMapping("/bookmarks/delete")
    public String deleteBookmark(@RequestParam("textId") Long textId, @AuthenticationPrincipal Customer customer) throws NotFoundException{
        Text text = textService.findText(textId); bookmarkService.deleteBookmark(text, customer);
        return "redirect:/show/" + textId;
    }
}
