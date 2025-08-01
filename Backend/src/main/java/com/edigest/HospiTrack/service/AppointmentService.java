package com.edigest.HospiTrack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.edigest.HospiTrack.entity.Appointment;
import com.edigest.HospiTrack.payload.AmbulanceRequestDTO;
import com.edigest.HospiTrack.payload.AppointmentDTO;
@Service
public class AppointmentService {

    @Autowired
    private JdbcTemplate jdbc;

    public Appointment save(Appointment appointment) {
        String sql = "INSERT INTO Appointments (id, patient_id, doctor_id, appointment_date, time_slot, type, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate() != null ? new java.sql.Date(appointment.getAppointmentDate().getTime()) : null,
                appointment.getTimeSlot(),
                appointment.getType(),
                appointment.getStatus()
        );
        return appointment;
    }

    public List<Appointment> getAll() {
        String sql = "SELECT id, patient_id, doctor_id, appointment_date, time_slot AS timeSlot, type, status FROM Appointments";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Appointment.class));
    }

    public void delete(String id) {
        String sql = "DELETE FROM Appointments WHERE id = ?";
        jdbc.update(sql, id);
    }

    public List<Appointment> getByPatientId(String patientId) {
        String sql = "SELECT id, patient_id, doctor_id, appointment_date, time_slot AS timeSlot, type, status FROM Appointments WHERE patient_id = ?";
        return jdbc.query(sql, new Object[]{patientId}, new BeanPropertyRowMapper<>(Appointment.class));
    }

    public List<AmbulanceRequestDTO> getAppointmentDetailsByPatientId(String patientId) {
        String sql = """
        SELECT a.id,
               a.doctor_id AS doctorId,
               u.name AS doctorName,
               dpt.id AS departmentId,
               dpt.name AS departmentName,
               spec.name AS specialization,
               TO_CHAR(a.appointment_date, 'YYYY-MM-DD') AS appointmentDate,
               a.time_slot AS timeSlot,
               a.status
        FROM Appointments a
        JOIN Doctors doc ON a.doctor_id = doc.id
        JOIN Users u ON doc.user_id = u.id
        LEFT JOIN Departments dpt ON doc.department_id = dpt.id
        LEFT JOIN Doctor_Specializations ds ON doc.id = ds.doctor_id
        LEFT JOIN Specializations spec ON ds.specialization_id = spec.id
        WHERE a.patient_id = ?
        ORDER BY a.appointment_date DESC, a.time_slot
    """;

        return jdbc.query(sql, new Object[]{patientId}, new BeanPropertyRowMapper<>(AmbulanceRequestDTO.class));
    }


    public String getPatientIdByUserId(String userId) {
        String sql = "SELECT id FROM Patients WHERE user_id = ?";
        List<String> ids = jdbc.query(sql, new Object[]{userId}, (rs, rowNum) -> rs.getString("id"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    public List<Appointment> getByUserId(String userId) {
        String patientId = getPatientIdByUserId(userId);
        if (patientId == null) {
            return List.of();
        }
        return getByPatientId(patientId);
    }

    public List<AppointmentDTO> getAllAppointmentsWithNames() {
        String sql = """
            SELECT a.id, a.patient_id, a.doctor_id, 
                   u_pat.name AS patient_name,
                   u_doc.name AS doctor_name,
                   a.appointment_date, a.time_slot, a.type, a.status
            FROM Appointments a
            LEFT JOIN Patients p ON a.patient_id = p.id
            LEFT JOIN Users u_pat ON p.user_id = u_pat.id
            LEFT JOIN Doctors d ON a.doctor_id = d.id
            LEFT JOIN Users u_doc ON d.user_id = u_doc.id
            ORDER BY a.appointment_date DESC
        """;
        
        return jdbc.query(sql, (rs, rowNum) -> new AppointmentDTO(
            rs.getString("id"),
            rs.getString("patient_id"),
            rs.getString("doctor_id"),
            rs.getString("patient_name"),
            rs.getString("doctor_name"),
            rs.getDate("appointment_date"),
            rs.getString("time_slot"),
            rs.getString("type"),
            rs.getString("status")
        ));
    }
}
