package ua.com.kisit.course_project.Controller.Web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Service.AuditService;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.CustomOAuth2UserService;
import ua.com.kisit.course_project.Service.CustomUserDetailsService;

@WebMvcTest(WebCarController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class WebCarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarService carService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private AuditService auditService;

    @Test
    void listCars_Success() throws Exception {
        Car car = new Car("Toyota", "Camry", 2020, "AA1111BB",
                Car.TransmissionType.AUTOMATIC, Car.FuelType.PETROL, 5, new BigDecimal("1000"));
        
        when(carService.getAllCarsPaginated(anyInt(), anyInt())).thenReturn(List.of(car));
        when(carService.getAllCars()).thenReturn(List.of(car));
        when(carService.getTotalCarsCount()).thenReturn(1L);

        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(view().name("cars/list"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void carDetails_Success() throws Exception {
        Car car = new Car("Toyota", "Camry", 2020, "AA1111BB",
                Car.TransmissionType.AUTOMATIC, Car.FuelType.PETROL, 5, new BigDecimal("1000"));
        car.setCarId(1L);

        when(carService.getCarById(1L)).thenReturn(Optional.of(car));

        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("cars/details"))
                .andExpect(model().attributeExists("car"));
    }

    @Test
    void carDetails_Redirects_WhenNotFound() throws Exception {
        when(carService.getCarById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cars/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/available"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showAddForm_WithAdminRole() throws Exception {
        mockMvc.perform(get("/cars/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("cars/form"))
                .andExpect(model().attributeExists("car"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void showAddForm_Redirects_WithClientRole() throws Exception {
        mockMvc.perform(get("/cars/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/available"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCar_Success() throws Exception {
        Car car = new Car();
        car.setCarId(5L);

        when(carService.addCar(any(Car.class))).thenReturn(car);

        mockMvc.perform(post("/cars/add")
                .param("brand", "Ford")
                .param("model", "Focus")
                .param("year", "2019")
                .param("registrationNumber", "CC1234XX")
                .param("seatsCount", "5")
                .param("dailyRate", "800")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/5"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCar_WithAdminRole() throws Exception {
        mockMvc.perform(post("/cars/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteCar_Redirects_WithManagerRole() throws Exception {
        mockMvc.perform(post("/cars/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/1"));
    }
}
