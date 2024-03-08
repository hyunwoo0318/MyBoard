package Lim.boardApp.domain.comment.controller;

import Lim.boardApp.common.exception.BindingResultHelper;
import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.domain.comment.form.CommentCreateForm;
import Lim.boardApp.domain.comment.service.CommentService;
import Lim.boardApp.domain.customer.entity.Customer;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 새로운 댓글을 추가 */
    @PostMapping("/text/{textId}/comments/new")
    public String postNewComment(
        @Valid @ModelAttribute("commentForm") CommentCreateForm commentCreateForm,
        BindingResult bindingResult,
        @PathVariable("textId") Long textId,
        @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer)
        throws NotFoundException {

        if (!bindingResult.hasErrors()) {
            return "redirect:/show/" + textId;
        }

        try {
            commentService.addComment(customer, commentCreateForm, textId);
        } catch (CustomException customException) {
            BindingResultHelper.setBindingResult(customException, bindingResult);
        }
        return "redirect:/show/" + textId;
    }
}
