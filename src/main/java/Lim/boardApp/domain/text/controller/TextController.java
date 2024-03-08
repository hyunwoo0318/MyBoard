package Lim.boardApp.domain.text.controller;

import Lim.boardApp.common.crawling.CrawlingService;
import Lim.boardApp.common.exception.BindingResultHelper;
import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.common.exception.UnauthorizedAccessException;
import Lim.boardApp.domain.comment.form.CommentCreateForm;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.dto.TextListQueryDto;
import Lim.boardApp.domain.text.dto.TextQueryDto;
import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.form.TextCreateForm;
import Lim.boardApp.domain.text.form.TextUpdateForm;
import Lim.boardApp.domain.text.service.TextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TextController {

    private final TextService textService;
    private final CrawlingService crawlingService;

    private void makePageBlock(Model model, Page<TextListQueryDto> pageTextList) {
        int startPage = Math.max(0, pageTextList.getNumber() - 2);
        int endPage = Math.min(pageTextList.getTotalPages(), startPage + 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
    }

    /** 키워드와 검색어의 종류를 입력받아 게시글 내에서 검색을 구현 */
    @GetMapping("/")
    public String searchText(
            @RequestParam(value = "searchKey", required = false) String searchKey,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "board-name", defaultValue = "전체", required = false) String boardName,
            @RequestParam(value = "textType", required = false) String textType,
            @RequestParam(value = "sort", defaultValue = "RECENT", required = false) String sort,
            Model model) {

        Page<TextListQueryDto> pageTextList =
                textService.queryTextListWithSearchAndSort(
                        page, searchKey, searchType, textType, boardName, sort);
        makePageBlock(model, pageTextList);

        model.addAttribute("pageForm", pageTextList);
        model.addAttribute("boardName", boardName);
        model.addAttribute("textType", textType);
        model.addAttribute("searchKey", searchKey);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sort", sort);

        return "board/textList";
    }

    /** 특정 글의 내용,제목,작성자,댓글,대댓글을 보여줌 */
    @GetMapping("/show/{textId}")
    public String showText(
            @PathVariable("textId") Long textId,
            @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer,
            Model model)
            throws NotFoundException {
        TextQueryDto textQueryDto = textService.showText(customer, textId);

        model.addAttribute("textDto", textQueryDto);
        model.addAttribute("commentForm", new CommentCreateForm());

        return "board/showText";
    }

    /** 게시글 생성, 수정, 삭제 */
    @GetMapping("/new")
    public String getNewText(Model model) {
        TextCreateForm textCreateForm = new TextCreateForm();
        model.addAttribute("text", textCreateForm);
        return "board/makeText";
    }

    @PostMapping("/new")
    public String postNewText(
            @Validated @ModelAttribute("text") TextCreateForm textCreateForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer) {
        if (bindingResult.hasErrors()) {
            return "board/makeText";
        }

        try {
            Text text = textService.createText(customer, textCreateForm);
            return "redirect:/show/" + text.getId();
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "board/makeText";
        }
    }

    @GetMapping("/edit/{textId}")
    public String getEditText(
            @PathVariable Long textId,
            Model model,
            @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer,
            BindingResult bindingResult) {

        try {
            TextUpdateForm textUpdateForm = textService.setTextUpdateForm(textId, customer);
            model.addAttribute("text", textUpdateForm);

            return "board/makeText";
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "redirect:/show/" + textId;
        } catch (UnauthorizedAccessException e) {
            return "redirect:/show/" + textId;
        }
    }

    @PostMapping("/edit/{textId}")
    public String postEditText(
            @Validated @ModelAttribute("text") TextUpdateForm textUpdateForm,
            BindingResult bindingResult,
            @PathVariable Long textId)
            throws NotFoundException {
        if (bindingResult.hasErrors()) {
            return "redirect:/edit/" + textId;
        }

        try {
            textService.updateText(textId, textUpdateForm);
            return "redirect:/show/" + textId;
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "redirect:/edit/" + textId;
        } catch (UnauthorizedAccessException e) {
            return "redirect:/show/" + textId;
        }
    }

    @PostMapping("/delete/{textId}")
    public String deleteText(
            @PathVariable Long textId,
            @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer,
            BindingResult bindingResult) {

        try {
            textService.deleteText(textId, customer);
            return "redirect:/";
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "redirect:/show/" + textId;
        } catch (UnauthorizedAccessException e) {
            return "redirect:/show/" + textId;
        }
    }

    /** 네이버 뉴스 크롤링 기능 */
    @GetMapping("/news")
    public String crawlingNews(
            @RequestParam("board-name") String boardName, RedirectAttributes attr) {
        try {
            crawlingService.crawlingNews(boardName);
            attr.addAttribute("board-name", boardName);
        } catch (CustomException | IOException customException) {
            attr.addAttribute("error", customException.getMessage());
        }
        return "redirect:/";
    }
}
