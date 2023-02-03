package Lim.boardApp.Controller;

import Lim.boardApp.PageConst;
import Lim.boardApp.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.TextRepository;
import Lim.boardApp.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final TextRepository textRepository;
    private final TextService textService;
    private final CustomerRepository customerRepository;

    //글 리스트 전체를 보여주는 페이지
    @GetMapping
    public String showTextList(@RequestParam(value = "page", defaultValue = "0") int page, Model model){

        int lastPage = textService.getLastPage(PageConst.PAGE_SIZE);

        if(page < 0) page = 0;
        if(lastPage < page) page = lastPage;

        PageRequest pageRequest = PageRequest.of(page, PageConst.PAGE_SIZE);
        Page<Text> pages = textRepository.findAll(pageRequest);
        List<Text> textList = pages.getContent();

        PageBlockForm block = textService.findBlock(page,lastPage, PageConst.PAGE_BLOCK_SIZE);

        model.addAttribute("textList", textList);
        model.addAttribute("page", page);
        model.addAttribute("block", block);

        return "board/textList";
    }

    //선택한 글의 정보를 보여줌
    @GetMapping("/{id}")
    public String showText(@PathVariable("id") Long id, Model model) {
        Text text = textRepository.getReferenceById(id);
        model.addAttribute(text);
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
        Optional<Customer> customerOptional = customerRepository.findById(id);
        //TODO : 예외처리
        Customer customer = customerOptional.get();
        Text text = new Text().builder()
                .title(textCreateForm.getTitle())
                .content(textCreateForm.getContent())
                .customer(customer)
                .build();
        textRepository.save(text);
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
        Optional<Text> textOp = textRepository.findById(id);
        if(textOp.isEmpty()) {
            System.out.println("id = " + id);
            return "redirect:/board";
        }
        Text text = textOp.get();
        text.updateText(textUpdateForm.getContent(), textUpdateForm.getTitle());
        textRepository.save(text);
        return "redirect:/board";
    }
}
