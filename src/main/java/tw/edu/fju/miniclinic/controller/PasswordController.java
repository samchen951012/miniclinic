package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tw.edu.fju.miniclinic.model.*;

@Controller
public class PasswordController {

    @Autowired
    private DoctorRepository doctorRepo;

    // 1. 顯示修改密碼畫面
    @GetMapping("/password")
    public String showPasswordForm(Model model) {
        model.addAttribute("passwordForm", new PasswordForm());
        return "password-change"; // 對應等一下要寫的 HTML 檔名
    }

    // 2. 處理修改密碼邏輯
    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") PasswordForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        // 檢查 A：格式檢查（如新密碼未滿 8 碼），若有錯直接退回頁面
        if (result.hasErrors()) {
            return "password-change";
        }

        // 檢查 B：檢查「新密碼」與「確認新密碼」是否相同
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "確認新密碼與新密碼不一致");
            return "password-change";
        }

        // 取得當前登入的醫師 ID
        String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");
        Doctor doctor = doctorRepo.findById(loggedInDoctorId).orElse(null);
        if (doctor == null) {
            return "redirect:/login";
        }

        // 檢查 C：利用 BCrypt 檢查輸入的「舊密碼」跟資料庫的「密碼雜湊值」是否吻合
        if (!BCrypt.checkpw(form.getOldPassword(), doctor.getPasswordHash())) {
            result.rejectValue("oldPassword", "error.oldPassword", "舊密碼輸入錯誤");
            return "password-change";
        }

        // 💡 通過所有考驗：將新密碼用 BCrypt 加密，並更新至資料庫
        String newHash = BCrypt.hashpw(form.getNewPassword(), BCrypt.gensalt());
        doctor.setPasswordHash(newHash);
        doctorRepo.save(doctor);

        // 成功提示，並清空表單
        model.addAttribute("successMessage", "密碼修改成功！");
        model.addAttribute("passwordForm", new PasswordForm());
        return "password-change";
    }
}