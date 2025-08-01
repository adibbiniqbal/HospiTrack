package com.edigest.HospiTrack.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.edigest.HospiTrack.entity.Doctor;

@Service
public class DoctorService {

    @Autowired
    private JdbcTemplate jdbc;

    public List<Doctor> getAllDoctors() {
        String sql = "SELECT * FROM Doctors";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Doctor.class));
    }

    public Doctor getDoctorById(String id) {
        String sql = "SELECT * FROM Doctors WHERE id = ?";
        List<Doctor> result = jdbc.query(sql, new BeanPropertyRowMapper<>(Doctor.class), id);
        return result.isEmpty() ? null : result.get(0);
    }

    public Doctor saveDoctor(Doctor doctor) {
        String sql = "INSERT INTO Doctors (id, user_id, branch_id, license_number, experience_years, available_hours, department_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                doctor.getId(),
                doctor.getUserId(),
                doctor.getBranchId(),
                doctor.getLicenseNumber(),
                doctor.getExperienceYears(),
                doctor.getAvailableHours(),
                doctor.getDepartmentId(),
                doctor.getImageUrl()
        );
        return doctor;
    }

    public Doctor updateDoctor(Doctor doctor) {
        String sql = "UPDATE Doctors SET user_id = ?, branch_id = ?, license_number = ?, " +
                "experience_years = ?, available_hours = ?, department_id = ?, image_url = ? WHERE id = ?";
        jdbc.update(sql,
                doctor.getUserId(),
                doctor.getBranchId(),
                doctor.getLicenseNumber(),
                doctor.getExperienceYears(),
                doctor.getAvailableHours(),
                doctor.getDepartmentId(),
                doctor.getImageUrl(),
                doctor.getId()
        );
        return doctor;
    }

    public void deleteDoctor(String id) {
        String sql = "DELETE FROM Doctors WHERE id = ?";
        jdbc.update(sql, id);
    }

    public int countDoctors() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'doctor'";
        Integer count = jdbc.queryForObject(sql, Integer.class);
        return (count != null) ? count : 0;
    }

    public List<Map<String, Object>> getAllDoctorsForFrontend() {
        String sql = """
            SELECT d.id AS doctorId,
                   u.name AS doctorName,
                   d.experience_years AS experienceYears,
                   d.license_number AS licenseNumber,
                   d.available_hours AS availableHours,
                   dep.name AS departmentName,
                   hb.name AS branchName,
                   d.image_url AS imageUrl
            FROM Doctors d
            JOIN Users u ON d.user_id = u.id
            JOIN Departments dep ON d.department_id = dep.id
            JOIN Hospital_Branches hb ON d.branch_id = hb.id
        """;

        return jdbc.queryForList(sql);
    }

    // method specifically for feedback form
    public List<Map<String, Object>> getDoctorsForFeedback() {
        String sql = """
            SELECT d.id AS doctorId,
                   u.name AS doctorName,
                   dep.name AS departmentName
            FROM Doctors d
            INNER JOIN Users u ON d.user_id = u.id
            INNER JOIN Departments dep ON d.department_id = dep.id
            WHERE u.name IS NOT NULL 
           -- AND TRIM(u.name) != ''--made life hell for feedback form
            ORDER BY u.name
        """;

        return jdbc.queryForList(sql);
    }
}