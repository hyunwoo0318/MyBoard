package Lim.boardApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    @Value("${admin.password}")
    String password;

    @GetMapping("/check")
    public String checkAdmin(Model model){
        String input = "";
        model.addAttribute("input", input);
        return "admin/check.html";
    }

    @PostMapping("/check")
    public String checkAdminPost(@ModelAttribute String input){
        if(input.equals(password)){
            return "../";
        }else{
            return "redirect:/check.html";
        }
    }
}
