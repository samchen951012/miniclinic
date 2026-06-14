package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, String> {

    // 既有的依姓名查詢方法
    List<Patient> findByName(String name);
}