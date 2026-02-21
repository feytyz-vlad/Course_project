package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.Payment.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepositoryImpl implements PaymentRepository {

    private final Connection connection;

    public PaymentRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, paymentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments (order_id, amount, payment_type, payment_method, " +
                "payment_status, transaction_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, payment.getOrderId());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentType().name());
            stmt.setString(4, payment.getPaymentMethod().name());
            stmt.setString(5, payment.getPaymentStatus().name());
            stmt.setString(6, payment.getTransactionId());
            stmt.setString(7, payment.getNotes());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    payment.setPaymentId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        String sql = "UPDATE payments SET order_id = ?, amount = ?, payment_type = ?, " +
                "payment_method = ?, payment_status = ?, transaction_id = ?, notes = ? " +
                "WHERE payment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, payment.getOrderId());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentType().name());
            stmt.setString(4, payment.getPaymentMethod().name());
            stmt.setString(5, payment.getPaymentStatus().name());
            stmt.setString(6, payment.getTransactionId());
            stmt.setString(7, payment.getNotes());
            stmt.setLong(8, payment.getPaymentId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return payment;
    }

    @Override
    public boolean deleteById(Long paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, paymentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        String sql = "SELECT * FROM payments WHERE payment_status = ?";
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<Payment> findByType(PaymentType type) {
        String sql = "SELECT * FROM payments WHERE payment_type = ?";
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public boolean updateStatus(Long paymentId, PaymentStatus newStatus) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setLong(2, paymentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
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
    }
}