package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.DamageReport;
import ua.com.kisit.course_project.Entity.DamageReport.RepairStatus;
import ua.com.kisit.course_project.Repository.CarRepository;
import ua.com.kisit.course_project.Repository.DamageReportRepository;

@ExtendWith(MockitoExtension.class)
class DamageReportServiceTest {

    @Mock
    private DamageReportRepository damageReportRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private DamageReportService damageReportService;

    @Test
    void createReport_Success() {
        DamageReport report = new DamageReport(10L, 1L, "Scratched bumper", LocalDate.now(), 5L);
        report.setRepairCost(new BigDecimal("200"));
        report.setFineAmount(new BigDecimal("50"));
        report.setReportId(100L);

        when(damageReportRepository.save(any(DamageReport.class))).thenReturn(report);

        DamageReport result = damageReportService.createReport(10L, 1L, "Scratched bumper", LocalDate.now(), new BigDecimal("200"), new BigDecimal("50"), 5L);

        assertNotNull(result);
        assertEquals(100L, result.getReportId());
        verify(damageReportRepository, times(1)).save(any(DamageReport.class));
        verify(carRepository, times(1)).updateStatus(1L, Car.CarStatus.DAMAGED);
    }

    @Test
    void setRepairCosts_Success() {
        DamageReport report = new DamageReport();
        report.setReportId(100L);

        when(damageReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(damageReportRepository.update(report)).thenReturn(report);

        boolean result = damageReportService.setRepairCosts(100L, new BigDecimal("400"), new BigDecimal("100"));

        assertTrue(result);
        assertEquals(new BigDecimal("400"), report.getRepairCost());
        assertEquals(new BigDecimal("100"), report.getFineAmount());
        verify(damageReportRepository, times(1)).update(report);
    }

    @Test
    void setRepairCosts_ThrowsException_WhenNotFound() {
        when(damageReportRepository.findById(100L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            damageReportService.setRepairCosts(100L, new BigDecimal("400"), new BigDecimal("100"));
        });

        assertEquals("Звіт не знайдено", exception.getMessage());
        verify(damageReportRepository, never()).update(any(DamageReport.class));
    }

    @Test
    void markAsInRepair_Success() {
        when(damageReportRepository.updateStatus(100L, RepairStatus.IN_REPAIR)).thenReturn(true);
        boolean result = damageReportService.markAsInRepair(100L);
        assertTrue(result);
        verify(damageReportRepository, times(1)).updateStatus(100L, RepairStatus.IN_REPAIR);
    }

    @Test
    void payDamage_Success() {
        DamageReport report = new DamageReport();
        report.setReportId(100L);
        report.setCarId(1L);

        when(damageReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(damageReportRepository.updateStatus(100L, RepairStatus.PAID)).thenReturn(true);

        boolean result = damageReportService.payDamage(100L);

        assertTrue(result);
        verify(damageReportRepository, times(1)).updateStatus(100L, RepairStatus.PAID);
        verify(carRepository, times(1)).updateStatus(1L, Car.CarStatus.AVAILABLE);
    }

    @Test
    void markAsCompleted_Success() {
        DamageReport report = new DamageReport();
        report.setReportId(100L);
        report.setCarId(1L);

        when(damageReportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(damageReportRepository.updateStatus(100L, RepairStatus.COMPLETED)).thenReturn(true);

        boolean result = damageReportService.markAsCompleted(100L);

        assertTrue(result);
        verify(damageReportRepository, times(1)).updateStatus(100L, RepairStatus.COMPLETED);
        verify(carRepository, times(1)).updateStatus(1L, Car.CarStatus.AVAILABLE);
    }

    @Test
    void getReportsByOrderId_Success() {
        List<DamageReport> reports = List.of(new DamageReport());
        when(damageReportRepository.findByOrderId(10L)).thenReturn(reports);

        List<DamageReport> result = damageReportService.getReportsByOrderId(10L);

        assertEquals(1, result.size());
    }

    @Test
    void getReportsByCarId_Success() {
        List<DamageReport> reports = List.of(new DamageReport());
        when(damageReportRepository.findByCarId(1L)).thenReturn(reports);

        List<DamageReport> result = damageReportService.getReportsByCarId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAllReports_Success() {
        List<DamageReport> reports = List.of(new DamageReport(), new DamageReport());
        when(damageReportRepository.findAll()).thenReturn(reports);

        List<DamageReport> result = damageReportService.getAllReports();

        assertEquals(2, result.size());
    }

    @Test
    void getReportById_Success() {
        DamageReport report = new DamageReport();
        report.setReportId(100L);
        when(damageReportRepository.findById(100L)).thenReturn(Optional.of(report));

        Optional<DamageReport> result = damageReportService.getReportById(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getReportId());
    }
}
