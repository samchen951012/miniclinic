package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    /**
     * 功能：醫師工作台畫面 (需要登入保護)
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String doctorId = (String) session.getAttribute("loggedInDoctorId");

        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // 撈取該登入醫師的所有掛號清單
        List<Appointment> appointments = appointmentRepo.findByDoctor(doctor);

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);

        return "dashboard";
    }

    /**
     * ✨【新功能一】全院預約掛號管理工作台 (需要登入保護)
     * 核心邏輯：撈取全院「所有」預約掛號資料，而不僅限於單一醫師
     */
    @GetMapping("/dashboard/all")
    public String allAppointmentsDashboard(HttpSession session, Model model) {
        String doctorId = (String) session.getAttribute("loggedInDoctorId");

        // 安全檢查：確保是已登入的醫師
        Doctor currentDoctor = doctorRepo.findById(doctorId).orElse(null);
        if (currentDoctor == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // 撈取全院所有掛號清單
        List<Appointment> allAppointments = appointmentRepo.findAll();

        model.addAttribute("doctor", currentDoctor);          // 用於畫面上方顯示當前登入醫師
        model.addAttribute("appointments", allAppointments);   // 傳遞全院掛號資料給前端

        return "dashboard_all"; // 渲染 templates/dashboard_all.html
    }

    /**
     * ✨【新功能一】處理掛號狀態更新 (需要登入保護)
     * 功能：點擊按鈕後變更指定掛號的狀態，支援更新為 COMPLETED 或 CANCELLED
     */
    @PostMapping("/appointments/update-status")
    public String updateAppointmentStatus(
            @RequestParam("apptId") Long apptId,
            @RequestParam("status") String status,
            HttpSession session) {
        
        String doctorId = (String) session.getAttribute("loggedInDoctorId");

        // 安全檢查：確保操作者是已登入的醫師
        if (doctorId == null || !doctorRepo.existsById(doctorId)) {
            session.invalidate();
            return "redirect:/login";
        }

        // 尋找對應的掛號紀錄並修改狀態
        Appointment appt = appointmentRepo.findById(apptId).orElse(null);
        if (appt != null) {
            appt.setStatus(status);
            appointmentRepo.save(appt); // 儲存變更回 SQLite 資料庫
        }

        // 狀態更新完成後，重導向回到全院管理工作台畫面
        return "redirect:/dashboard/all";
    }

    /**
     * 功能：轉導至獨立的統計資料網頁範本 (公開路由)
     */
    @GetMapping("/stats")
    public String showStatsPage() {
        return "stats"; // 渲染 templates/stats.html
    }

    /**
     * 功能：符合期末作業規範之統計摘要 API (公開 API)
     * 透過 JPA 的 count() 查詢實作，提供前端非同步 fetch 呼叫
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getClinicStatsJson() {
        long totalDoctors = doctorRepo.count();
        long totalPatients = patientRepo.count();
        long totalAppointments = appointmentRepo.count();

        // 依狀態分組計算 (直接使用現有 Repository 的計算邏輯或計算方法)
        long bookedCount = appointmentRepo.countByStatus("BOOKED");
        long completedCount = appointmentRepo.countByStatus("COMPLETED");
        long cancelledCount = appointmentRepo.countByStatus("CANCELLED");

        // 嚴格組合作業規範要求的 JSON 結構
        Map<String, Object> byStatus = new LinkedHashMap<>();
        byStatus.put("BOOKED", bookedCount);
        byStatus.put("COMPLETED", completedCount);
        byStatus.put("CANCELLED", cancelledCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDoctors", totalDoctors);
        result.put("totalPatients", totalPatients);
        result.put("totalAppointments", totalAppointments);
        result.put("byStatus", byStatus);

        return ResponseEntity.ok(result);
    }
}