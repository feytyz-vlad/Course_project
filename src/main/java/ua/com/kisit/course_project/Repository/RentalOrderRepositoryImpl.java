package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.RentalOrder.OrderStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository  // FIXED: цей файл був відсутній повністю — створено з нуля
public class RentalOrderRepositoryImpl implements RentalOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public RentalOrderRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RentalOrder> orderRowMapper = (rs, rowNum) -> {
        RentalOrder order = new RentalOrder();
        order.setOrderId(rs.getLong("order_id"));
        order.setClientId(rs.getLong("client_id"));
        order.setCarId(rs.getLong("car_id"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) order.setStartDate(startDate.toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) order.setEndDate(endDate.toLocalDate());
        Date actualReturnDate = rs.getDate("actual_return_date");
        if (actualReturnDate != null) order.setActualReturnDate(actualReturnDate.toLocalDate());
        order.setTotalDays(rs.getInt("total_days"));
        order.setDailyRate(rs.getBigDecimal("daily_rate"));
        order.setTotalCost(rs.getBigDecimal("total_cost"));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                order.setStatus(OrderStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                order.setStatus(OrderStatus.PENDING);
            }
        } else {
            order.setStatus(OrderStatus.PENDING);
        }
        order.setRejectionReason(rs.getString("rejection_reason"));
        order.setAdditionalNotes(rs.getString("additional_notes"));
        try {
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) order.setCreatedAt(createdAt.toLocalDateTime());
        } catch (Exception e) { /* Column might not exist */ }
        
        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) order.setUpdatedAt(updatedAt.toLocalDateTime());
        } catch (Exception e) { /* Column might not exist */ }
        
        return order;
    };

    @Override
    public RentalOrder save(RentalOrder order) {
        String sql = "INSERT INTO rental_orders (client_id, car_id, start_date, end_date, " +
                "total_days, daily_rate, total_cost, status, additional_notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getClientId());
            ps.setLong(2, order.getCarId());
            ps.setDate(3, Date.valueOf(order.getStartDate()));
            ps.setDate(4, Date.valueOf(order.getEndDate()));
            ps.setInt(5, order.getTotalDays());
            ps.setBigDecimal(6, order.getDailyRate());
            ps.setBigDecimal(7, order.getTotalCost());
            ps.setString(8, order.getStatus().name());
            ps.setString(9, order.getAdditionalNotes());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            order.setOrderId(keyHolder.getKey().longValue());
        }
        return order;
    }

    @Override
    public Optional<RentalOrder> findById(Long orderId) {
        List<RentalOrder> result = jdbcTemplate.query(
                "SELECT * FROM rental_orders WHERE order_id=?", orderRowMapper, orderId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<RentalOrder> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM rental_orders", orderRowMapper);
    }

    @Override
    public List<RentalOrder> findByClientId(Long clientId) {
        return jdbcTemplate.query(
                "SELECT * FROM rental_orders WHERE client_id=?",
                orderRowMapper, clientId);
    }

    @Override
    public List<RentalOrder> findByClientIdAndStatus(Long clientId, OrderStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM rental_orders WHERE client_id=? AND status=? ORDER BY created_at DESC",
                orderRowMapper, clientId, status.name());
    }

    @Override
    public List<RentalOrder> findPendingOrders() {
        return jdbcTemplate.query(
                "SELECT * FROM rental_orders WHERE status='PENDING' ORDER BY created_at ASC",
                orderRowMapper);
    }

    @Override
    public List<RentalOrder> findActiveOrders() {
        return jdbcTemplate.query(
                "SELECT * FROM rental_orders WHERE status='ACTIVE' ORDER BY start_date ASC",
                orderRowMapper);
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus status) {
        return jdbcTemplate.update(
                "UPDATE rental_orders SET status=? WHERE order_id=?",
                status.name(), orderId) > 0;
    }

    @Override
    public boolean reject(Long orderId, String reason) {
        return jdbcTemplate.update(
                "UPDATE rental_orders SET status='REJECTED', rejection_reason=? WHERE order_id=?",
                reason, orderId) > 0;
    }

    @Override
    public boolean updateActualReturnDate(Long orderId, LocalDate actualReturnDate) {
        return jdbcTemplate.update(
                "UPDATE rental_orders SET actual_return_date=? WHERE order_id=?",
                Date.valueOf(actualReturnDate), orderId) > 0;
    }

    @Override
    public boolean deleteById(Long orderId) {
        return jdbcTemplate.update("DELETE FROM rental_orders WHERE order_id=?", orderId) > 0;
    }
}