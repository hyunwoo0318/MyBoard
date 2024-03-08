package Lim.boardApp.domain.bookmark.controller;

import Lim.boardApp.common.exception.BindingResultHelper;
import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.domain.bookmark.service.BookmarkService;
import Lim.boardApp.domain.customer.entity.Customer;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    /** 북마크 실행, 취소 */
    @PostMapping("/bookmarks/new")
    public String postNewBookmark(
        @RequestParam("textId") Long textId,
        BindingResult bindingResult,
        @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer)
        throws NotFoundException {
        try {
            bookmarkService.addBookmark(textId, customer);
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
        }
        return "redirect:/show/" + textId;
    }

    @PostMapping("/bookmarks/delete")
    public String deleteBookmark(
        @RequestParam("textId") Long textId,
        BindingResult bindingResult,
        @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer)
        throws NotFoundException {
        try {
            bookmarkService.deleteBookmark(textId, customer);
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
        }

        return "redirect:/show/" + textId;
    }
}
