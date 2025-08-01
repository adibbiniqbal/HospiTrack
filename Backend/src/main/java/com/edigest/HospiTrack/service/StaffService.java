package com.edigest.HospiTrack.service;

import java.util.List;
import java.util.Random;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.edigest.HospiTrack.payload.StaffDTO;
import com.edigest.HospiTrack.payload.StaffRowMapper;

@Service
public class StaffService {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    public StaffService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StaffDTO> getAllStaff() {
        String sql = """
            SELECT s.id, u.name, u.email, u.phone, s.designation,
                   d.name AS department, b.name AS location
            FROM Staff s
            JOIN Users u ON s.user_id = u.id
            LEFT JOIN Departments d ON s.department_id = d.id
            LEFT JOIN Hospital_Branches b ON s.branch_id = b.id
        """;

        List<StaffDTO> staffList = jdbcTemplate.query(sql, new StaffRowMapper());

        // Add fake data fields for UI consistency
        for (StaffDTO staff : staffList) {
            staff.setExperience(random.nextInt(15) + 1);
            String[] shifts = {"Morning", "Evening", "Night"};
            staff.setShift(shifts[random.nextInt(shifts.length)]);
            staff.setImageUrl(null); // or set a default image URL here
            staff.setRating(String.format("%.1f", 4 + random.nextDouble()));
        }

        return staffList;
    }

    public StaffDTO getStaffById(String id) {
        try {
            String sql = """
                SELECT s.id, u.name, u.email, u.phone, s.designation,
                       d.name AS department, b.name AS location
                FROM Staff s
                JOIN Users u ON s.user_id = u.id
                LEFT JOIN Departments d ON s.department_id = d.id
                LEFT JOIN Hospital_Branches b ON s.branch_id = b.id
                WHERE s.id = ?
            """;
            
            List<StaffDTO> results = jdbcTemplate.query(sql, new StaffRowMapper(), id);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching staff by ID: " + e.getMessage());
        }
    }

    public boolean deleteStaff(String id) {
        try {
            // Actual delete since there's no active column
            String sql = "DELETE FROM Staff WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            return rowsAffected > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting staff: " + e.getMessage());
        }
    }
}
