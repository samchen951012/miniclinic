package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.LoginForm; 

@Controller
public class LoginController {

    @Autowired
    private DoctorRepository doctorRepository;

    // GET：顯示登入頁
    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    // POST：處理登入
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        // 步驟 1：檢查表單驗證
        if (result.hasErrors()) {
            return "login";  // 顯示錯誤訊息
        }

        // 步驟 2：查詢醫師
        Doctor doctor = doctorRepository.findById(form.getDoctorId()).orElse(null);

        // 步驟 3：檢查密碼
        if (doctor == null || !BCrypt.checkpw(form.getPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "醫師編號或密碼錯誤");
            return "login";
        }

        // 步驟 4：登入成功，存入 Session
        session.setAttribute("loggedInDoctorId", doctor.getDoctorId());
        session.setAttribute("loggedInDoctorName", doctor.getName());

        return "redirect:/dashboard";
    }

    // 登出
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 清除 Session
        return "redirect:/login";
    }
}