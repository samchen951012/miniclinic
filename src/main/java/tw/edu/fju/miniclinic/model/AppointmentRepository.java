package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 既有的查詢方法（完整保留）
    List<Appointment> findByApptDate(LocalDate apptDate);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctorAndApptDate(Doctor doctor, LocalDate apptDate);
    long countByApptDateBetween(LocalDate from, LocalDate to);
    
    // ✨ 關鍵修正：對應 DashboardController 內功能二的狀態統計
    // Spring Data JPA 會自動將其轉化為：SELECT COUNT(*) FROM appointment WHERE status = ?
    long countByStatus(String status); 
}