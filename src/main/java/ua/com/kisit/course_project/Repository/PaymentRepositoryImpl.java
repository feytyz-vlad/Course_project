package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.Payment.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getLong("payment_id"));
        payment.setOrderId(rs.getLong("order_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentType(PaymentType.valueOf(rs.getString("payment_type")));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setNotes(rs.getString("notes"));

        Timestamp paymentTimestamp = rs.getTimestamp("payment_date");
        if (paymentTimestamp != null) {
            payment.setPaymentDate(paymentTimestamp.toLocalDateTime());
        }

        return payment;
    };

    @Override
    public Optional<Payment> findById(Long paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        List<Payment> result = jdbcTemplate.query(sql, paymentRowMapper, paymentId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments (order_id, amount, payment_type, payment_method, " +
                "payment_status, transaction_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, payment.getOrderId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentType().name());
            ps.setString(4, payment.getPaymentMethod().name());
            ps.setString(5, payment.getPaymentStatus().name());
            ps.setString(6, payment.getTransactionId());
            ps.setString(7, payment.getNotes());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            payment.setPaymentId(keyHolder.getKey().longValue());
        }
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        String sql = "UPDATE payments SET order_id = ?, amount = ?, payment_type = ?, " +
                "payment_method = ?, payment_status = ?, transaction_id = ?, notes = ? " +
                "WHERE payment_id = ?";

        jdbcTemplate.update(sql,
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentType().name(),
                payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getTransactionId(),
                payment.getNotes(),
                payment.getPaymentId());
        return payment;
    }

    @Override
    public boolean deleteById(Long paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        return jdbcTemplate.update(sql, paymentId) > 0;
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        return jdbcTemplate.query(sql, paymentRowMapper);
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, orderId);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        String sql = "SELECT * FROM payments WHERE payment_status = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, status.name());
    }

    @Override
    public List<Payment> findByType(PaymentType type) {
        String sql = "SELECT * FROM payments WHERE payment_type = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, type.name());
    }

    @Override
    public boolean updateStatus(Long paymentId, PaymentStatus newStatus) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, newStatus.name(), paymentId) > 0;
    }
}