package tw.edu.fju.miniclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // 回傳 home 代表尋找 templates/home.html
        return "home";
    }
}