package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import tw.edu.fju.miniclinic.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AppointmentApiController {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    // 🎯 需求 1：GET /api/appointments/count -> 回傳總掛號數 JSON
    @GetMapping("/api/appointments/count")
    public Map<String, Long> getAppointmentCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", appointmentRepo.count());
        return response;
    }

    // 🎯 需求 2：GET /api/appointments (支援 date 與 doctorId 的多條件選填篩選)
    @GetMapping("/api/appointments")
    public List<Appointment> getAppointments(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String doctorId) {

        // 情況 A：如果同時傳、或只傳了日期
        if (date != null && !date.isBlank()) {
            return appointmentRepo.findByApptDate(LocalDate.parse(date));
        }

        // 情況 B：如果只傳了醫師 ID
        if (doctorId != null && !doctorId.isBlank()) {
            Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
            if (doctor != null) {
                return appointmentRepo.findByDoctor(doctor);
            }
        }

        // 情況 C：什麼都沒傳，回傳全部掛號紀錄
        return appointmentRepo.findAll();
    }

    // 🎯 需求 3：大數據面板專用 API (回傳所有大指標與科別分組)
    @GetMapping("/api/dashboard/data")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalDoctors", doctorRepo.count());     // 醫師總數
        data.put("totalPatients", patientRepo.count());   // 病患總數
        data.put("totalAppointments", appointmentRepo.count()); // 掛號總數
        return data;

        }

    @PutMapping("/api/appointments/{apptId}/status")
public ResponseEntity<Appointment> updateStatus(
		@PathVariable Long apptId,
		@RequestBody Map<String, String> payload,
		HttpSession session) {

	String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");

	Appointment appt = appointmentRepo.findById(apptId).orElse(null);
	if (appt == null) {
		return ResponseEntity.notFound().build();
	}

	// 只能修改自己的掛號
	if (!appt.getDoctor().getDoctorId().equals(loggedInDoctorId)) {
		return ResponseEntity.status(403).build();
	}

	String newStatus = payload.get("status");
	if (!List.of("BOOKED", "COMPLETED", "CANCELLED").contains(newStatus)) {
		return ResponseEntity.badRequest().build();
	}

	appt.setStatus(newStatus);
	return ResponseEntity.ok(appointmentRepo.save(appt));
}
}