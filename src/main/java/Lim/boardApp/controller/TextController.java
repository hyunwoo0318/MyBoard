package Lim.boardApp.controller;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.domain.*;
import Lim.boardApp.dto.TextQueryDto;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TextController {

    private final TextService textService;
    private final BookmarkService bookmarkService;
    private final CrawlingService crawlingService;

    /**
     * 게시글 리스트를 보여줌 -> 페이징 과정을 거침
     */
    @GetMapping("/")
    public String showTextList(@RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "board-name", defaultValue = "전체") String boardName,
                               @RequestParam(value = "textType", required = false) String textType,
                               Model model){

        PageForm pageForm = textService.pagingByAll(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, boardName, textType);
        model.addAttribute("boardName", boardName);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", "");
        model.addAttribute("searchType", "");
        return "board/textList";
    }

    /**
     * 키워드와 검색어의 종류를 입력받아 게시글 내에서 검색을 구현
     */
    @GetMapping("/search")
    public String searchText(@RequestParam(value = "searchKey") String searchKey,
                             @RequestParam(value = "searchType", required = false) String searchType,
                             @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                             @RequestParam(value = "board-name", defaultValue = "전체", required = false)String boardName,
                             Model model) {

        PageForm pageForm = textService.pagingBySearch(page, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, searchKey, searchType ,boardName);
        model.addAttribute("boardName", boardName);
        model.addAttribute("pageForm", pageForm);
        model.addAttribute("searchKey", "");
        return "board/textList";
    }


    /**
     * 특정 글의 내용,제목,작성자,댓글,대댓글을 보여줌
    */
    @GetMapping("show/{textId}")
    public String showText(@PathVariable("textId") Long textId, @AuthenticationPrincipal Customer customer, Model model) throws NotFoundException {

        TextQueryDto textQueryDto = textService.showText(customer, textId);
        boolean textOwn = textService.isOwner(textId, customer.getId());
//
//        //조회수 상승
//        if (!textOwn) {
//            textService.increaseViewCnt(text,customer.getId());
//        }
//
//        //글을 조회하는 사람이 해당 글을 북마크했는지 확인
//        boolean isBookmarked = bookmarkService.isBookmarked(textId, customerId);
//
//        List<Hashtag> hashtagList = textService.findHashtagList(text);
//        List<Comment> commentList = textService.findParentCommentList(text);
//        int commentCnt = textService.findCommentCnt(text.getId());


        model.addAttribute("textDto",textQueryDto);
        model.addAttribute("commentForm", new CommentForm());


        return "board/showText";
    }

    /**
     * 게시글 생성, 수정, 삭제
     */

    @GetMapping("/new")
    public String getNewText(Model model) {
        TextCreateForm textCreateForm = new TextCreateForm();
        model.addAttribute("text", textCreateForm);
        return "board/makeText";
    }

    @PostMapping("/new")
    public String postNewText(@Validated @ModelAttribute("text") TextCreateForm textCreateForm, BindingResult bindingResult,
                              @AuthenticationPrincipal Customer customer) throws IOException {
        if (bindingResult.hasErrors()) {
            return "board/makeText";
        }

        Text text = textService.createText(customer, textCreateForm);

        return "redirect:/show/" + text.getId();
    }

    @GetMapping("edit/{textId}")
    public String getEditText(@PathVariable Long textId, Model model, @AuthenticationPrincipal Customer customer, HttpServletResponse response) throws NotFoundException, IOException {
        Boolean ownerCheck = textService.isOwner(textId, customer.getId());

        //수정 요청한 회원이 글 작성 회원과 다른 경우 403 return
        if (!ownerCheck) {
            response.sendError(403);
        }else{
            TextUpdateForm textUpdateForm = textService.setTextUpdateForm(textId);
            model.addAttribute("text", textUpdateForm);
        }
        return "board/makeText";
    }

    @PostMapping("edit/{textId}")
    public String postEditText(@Validated @ModelAttribute("text") TextUpdateForm textUpdateForm, BindingResult bindingResult, @PathVariable Long textId) throws NotFoundException {
        if (bindingResult.hasErrors()) {
            return "redirect:/edit/" + textId;
        }
        textService.updateText(textId, textUpdateForm);
        return "redirect:/show/" + textId;
    }

    @PostMapping("delete/{textId}")
    public String deleteText(@PathVariable Long textId, @AuthenticationPrincipal Customer customer,HttpServletResponse response) throws NotFoundException, IOException {
        Boolean ownerCheck = textService.isOwner(textId, customer.getId());

        //수정 요청한 회원이 글 작성 회원과 다른 경우 403 return
        if (!ownerCheck) {
            response.sendError(403);
            return "error/403";
        }else{
            textService.deleteText(textId);
            return "redirect:/";
        }
    }

    /**
     * 새로운 댓글을 추가
     */
    @PostMapping("text/{textId}/comments/new")
    public String postNewComment(@Validated @ModelAttribute("commentForm")CommentForm commentForm, BindingResult bindingResult,
                                 @PathVariable("textId") Long textId, @AuthenticationPrincipal Customer customer) throws NotFoundException {
        if (!bindingResult.hasErrors()) {
            textService.addComment(customer, commentForm, textId);
        }
        return "redirect:/show/" + textId;
    }

    /**
     * 북마크 실행, 취소
     */

    @PostMapping("/bookmarks/new")
    public String postNewBookmark(@RequestParam("textId") Long textId, @AuthenticationPrincipal Customer customer) throws NotFoundException{
        bookmarkService.addBookmark(textId, customer);
        return "redirect:/show/" + textId;

    }

    @PostMapping("/bookmarks/delete")
    public String deleteBookmark(@RequestParam("textId") Long textId, @AuthenticationPrincipal Customer customer) throws NotFoundException{
        bookmarkService.deleteBookmark(textId, customer);
        return "redirect:/show/" + textId;
    }

    /**
     * 네이버 뉴스 크롤링 기능
     */
    @GetMapping("/news")
    public String crawlingNews(@RequestParam("board-name") String boardName, RedirectAttributes attr) throws IOException {
        crawlingService.crawlingNews(boardName);
        attr.addAttribute("board-name", boardName);
        return "redirect:/";
    }
}
