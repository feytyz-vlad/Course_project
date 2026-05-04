package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.DamageReport;
import ua.com.kisit.course_project.Entity.DamageReport.RepairStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class DamageReportRepositoryImpl implements DamageReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public DamageReportRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DamageReport> rowMapper = (rs, rowNum) -> {
        DamageReport report = new DamageReport();
        report.setReportId(rs.getLong("report_id"));
        report.setOrderId(rs.getLong("order_id"));
        report.setCarId(rs.getLong("car_id"));
        report.setDamageDescription(rs.getString("damage_description"));
        Date damageDate = rs.getDate("damage_date");
        if (damageDate != null) report.setDamageDate(damageDate.toLocalDate());
        report.setRepairCost(rs.getBigDecimal("repair_cost"));
        report.setFineAmount(rs.getBigDecimal("fine_amount"));
        String status = rs.getString("repair_status");
        if (status != null) {
            try {
                report.setRepairStatus(RepairStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                report.setRepairStatus(RepairStatus.ASSESSED);
            }
        } else {
            report.setRepairStatus(RepairStatus.ASSESSED);
        }
        report.setPhotosUrl(rs.getString("photos_url"));
        report.setCreatedByUserId(rs.getLong("created_by_user_id"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) report.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) report.setUpdatedAt(updatedAt.toLocalDateTime());
        return report;
    };

    @Override
    public Optional<DamageReport> findById(Long reportId) {
        List<DamageReport> result = jdbcTemplate.query(
                "SELECT * FROM damage_reports WHERE report_id = ?", rowMapper, reportId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public DamageReport save(DamageReport report) {
        String sql = "INSERT INTO damage_reports (order_id, car_id, damage_description, " +
                "damage_date, repair_cost, fine_amount, repair_status, photos_url, created_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, report.getOrderId());
            ps.setLong(2, report.getCarId());
            ps.setString(3, report.getDamageDescription());
            ps.setDate(4, report.getDamageDate() != null ? Date.valueOf(report.getDamageDate()) : null);
            ps.setBigDecimal(5, report.getRepairCost());
            ps.setBigDecimal(6, report.getFineAmount());
            ps.setString(7, report.getRepairStatus() != null ? report.getRepairStatus().name() : RepairStatus.ASSESSED.name());
            ps.setString(8, report.getPhotosUrl());
            ps.setLong(9, report.getCreatedByUserId() != null ? report.getCreatedByUserId() : 0);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            report.setReportId(keyHolder.getKey().longValue());
        }
        return report;
    }

    @Override
    public DamageReport update(DamageReport report) {
        jdbcTemplate.update(
                "UPDATE damage_reports SET damage_description=?, damage_date=?, " +
                        "repair_cost=?, fine_amount=?, repair_status=?, photos_url=? WHERE report_id=?",
                report.getDamageDescription(),
                report.getDamageDate() != null ? Date.valueOf(report.getDamageDate()) : null,
                report.getRepairCost(),
                report.getFineAmount(),
                report.getRepairStatus() != null ? report.getRepairStatus().name() : null,
                report.getPhotosUrl(),
                report.getReportId());
        return report;
    }

    @Override
    public boolean deleteById(Long reportId) {
        return jdbcTemplate.update("DELETE FROM damage_reports WHERE report_id=?", reportId) > 0;
    }

    @Override
    public List<DamageReport> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM damage_reports ORDER BY created_at DESC", rowMapper);
    }

    @Override
    public List<DamageReport> findByOrderId(Long orderId) {
        return jdbcTemplate.query(
                "SELECT * FROM damage_reports WHERE order_id=?", rowMapper, orderId);
    }

    @Override
    public List<DamageReport> findByCarId(Long carId) {
        return jdbcTemplate.query(
                "SELECT * FROM damage_reports WHERE car_id=?", rowMapper, carId);
    }

    @Override
    public List<DamageReport> findByStatus(RepairStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM damage_reports WHERE repair_status=?", rowMapper, status.name());
    }

    @Override
    public boolean updateStatus(Long reportId, RepairStatus newStatus) {
        return jdbcTemplate.update(
                "UPDATE damage_reports SET repair_status=? WHERE report_id=?",
                newStatus.name(), reportId) > 0;
    }
}