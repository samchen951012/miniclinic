package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Map;

import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.AppointmentForm;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.Patient;
import tw.edu.fju.miniclinic.model.PatientRepository;

@Controller
public class AppointmentController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    // GET：顯示表單
    @GetMapping("/appointment/new")
    public String newAppointmentForm(Model model) {
        model.addAttribute("form", new AppointmentForm());
        model.addAttribute("doctors", doctorRepo.findAll());
        return "appointment-new";
    }

    // AppointmentController.java
    @PostMapping("/appointment/new")
    public String submitAppointment(
            @Valid @ModelAttribute("form") AppointmentForm form,   // ← 加 @Valid
            BindingResult result,                          // ← 緊接在 @Valid 參數之後
            Model model) {

    if (result.hasErrors()) {
		model.addAttribute("form", form);
		model.addAttribute("doctors", doctorRepo.findAll());
		return "appointment-new";
	}

        // 步驟 1：用表單的字串 ID，從資料庫查出真正的物件
        Patient patient = patientRepo.findById(form.getChartNo()).orElse(null);
        Doctor  doctor  = doctorRepo.findById(form.getDoctorId()).orElse(null);

        // 步驟 2：驗證——找不到就回表單顯示錯誤
        if (patient == null || doctor == null) {
            model.addAttribute("error", "查無此病歷號或醫師，請確認後重試");
            model.addAttribute("form", form);
            model.addAttribute("doctors", doctorRepo.findAll());
            return "appointment-new";   // ← 回到表單頁，不是跳轉
        }

        // 步驟 3：建立 Appointment Entity，設定關聯物件
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);

        try {
            appt.setApptDate(LocalDate.parse(form.getApptDate()));
        } catch (Exception e) {
            model.addAttribute("error", "日期格式錯誤，請使用 YYYY-MM-DD");
            model.addAttribute("form", form);
            model.addAttribute("doctors", doctorRepo.findAll());
            return "appointment-new";
        }

        appt.setTimeSlot(form.getTimeSlot());
        appt.setStatus("BOOKED");

        // 步驟 4：存入資料庫，JPA 自動填入 apptId
        Appointment saved = appointmentRepo.save(appt);

        // 步驟 5：把儲存後的物件交給結果頁面
        model.addAttribute("appointment", saved);
        return "appointment-result";
    }

    @PostMapping("/api/appointments")
    public ResponseEntity<Appointment> createAppointment(
            @RequestBody Map<String, String> request) {

        // 從 request 取出資料
        String chartNo = request.get("chartNo");
        String doctorId = request.get("doctorId");

        LocalDate apptDate;
        try {
            apptDate = LocalDate.parse(request.get("apptDate"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        String timeSlot = request.get("timeSlot");

        // 查詢關聯的 Patient 與 Doctor
        Patient patient = patientRepo.findById(chartNo).orElse(null);
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (patient == null || doctor == null) {
            return ResponseEntity.badRequest().build();
        }

        // 建立 Appointment 物件
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setApptDate(apptDate);
        appt.setTimeSlot(timeSlot);
        appt.setStatus("BOOKED");

        Appointment saved = appointmentRepo.save(appt);
        return ResponseEntity.status(201).body(saved);
    }
}