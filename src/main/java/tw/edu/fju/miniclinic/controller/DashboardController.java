package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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