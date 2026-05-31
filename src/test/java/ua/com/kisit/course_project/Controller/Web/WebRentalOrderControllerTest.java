package ua.com.kisit.course_project.Controller.Web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.AuditService;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.ClientService;
import ua.com.kisit.course_project.Service.CustomOAuth2UserService;
import ua.com.kisit.course_project.Service.CustomUserDetailsService;
import ua.com.kisit.course_project.Service.DamageReportService;
import ua.com.kisit.course_project.Service.PaymentService;
import ua.com.kisit.course_project.Service.RentalOrderService;

@WebMvcTest(WebRentalOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebRentalOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalOrderService orderService;

    @MockBean
    private CarService carService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DamageReportService damageService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private AuditService auditService;

    /** Helper to create a fully filled CLIENT with rnokpp, phone, firstName set */
    private Client buildFullClient(Long userId, Long clientId) {
        Client client = new Client(userId, "John", "Doe", "AB", "123456", "+380501111111", "LIC123");
        client.setClientId(clientId);
        client.setRnokpp("1234567890");
        return client;
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void listOrders_Admin_Success() throws Exception {
        User adminUser = new User("admin@test.com", "hash", UserRole.ADMIN);
        adminUser.setUserId(1L);

        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @WithMockUser(username = "client@test.com")
    void listOrders_Client_Success() throws Exception {
        User clientUser = new User("client@test.com", "hash", UserRole.CLIENT);
        clientUser.setUserId(2L);
        Client client = buildFullClient(2L, 50L);

        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);

        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));
        when(clientService.getClientByUserId(2L)).thenReturn(Optional.of(client));
        when(orderService.getClientOrders(50L)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("title", "Мої замовлення"));
    }

    @Test
    @WithMockUser(username = "client@test.com")
    void showCreateForm_Success() throws Exception {
        User clientUser = new User("client@test.com", "hash", UserRole.CLIENT);
        clientUser.setUserId(2L);
        // Must have rnokpp, phone, firstName - otherwise controller redirects to /profile/complete
        Client client = buildFullClient(2L, 50L);

        Car car = new Car();
        car.setCarId(5L);

        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));
        when(clientService.getClientByUserId(2L)).thenReturn(Optional.of(client));
        when(carService.getCarById(5L)).thenReturn(Optional.of(car));

        mockMvc.perform(get("/orders/create").param("carId", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/create"))
                .andExpect(model().attribute("car", car));
    }

    @Test
    @WithMockUser(username = "client@test.com")
    void createOrder_Success() throws Exception {
        User clientUser = new User("client@test.com", "hash", UserRole.CLIENT);
        clientUser.setUserId(2L);
        Client client = buildFullClient(2L, 50L);

        RentalOrder order = new RentalOrder();
        order.setOrderId(100L);

        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));
        when(clientService.getClientByUserId(2L)).thenReturn(Optional.of(client));
        when(orderService.createOrder(eq(50L), eq(5L), any(LocalDate.class), any(LocalDate.class))).thenReturn(order);

        mockMvc.perform(post("/orders/create")
                .param("carId", "5")
                .param("startDate", "2026-06-01")
                .param("endDate", "2026-06-05")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/checkout/100"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Замовлення створено! Будь ласка, оплатіть оренду."));

        verify(orderService, times(1)).createOrder(eq(50L), eq(5L), eq(LocalDate.of(2026, 6, 1)), eq(LocalDate.of(2026, 6, 5)));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void approveOrder_Admin_Success() throws Exception {
        mockMvc.perform(post("/orders/10/approve")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Замовлення затверджено!"));

        verify(orderService, times(1)).approveOrder(10L);
    }

    @Test
    @WithMockUser(username = "client@test.com")
    void approveOrder_NonAdmin_Redirects() throws Exception {
        mockMvc.perform(post("/orders/10/approve")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(orderService, never()).approveOrder(anyLong());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void rejectOrder_Admin_Success() throws Exception {
        mockMvc.perform(post("/orders/10/reject")
                .param("reason", "Invalid documents")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Замовлення відхилено!"));

        verify(orderService, times(1)).rejectOrder(10L, "Invalid documents");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void completeOrder_Admin_Success() throws Exception {
        mockMvc.perform(post("/orders/10/complete")
                .param("actualReturnDate", "2026-06-10")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Замовлення завершено!"));

        verify(orderService, times(1)).completeOrder(10L, LocalDate.of(2026, 6, 10));
    }

    @Test
    @WithMockUser(username = "client@test.com")
    void orderDetails_Client_Success() throws Exception {
        User clientUser = new User("client@test.com", "hash", UserRole.CLIENT);
        clientUser.setUserId(2L);
        Client client = buildFullClient(2L, 50L);

        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);
        order.setClientId(50L);

        when(orderService.getOrderById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(clientUser));
        when(clientService.getClientByUserId(2L)).thenReturn(Optional.of(client));
        when(damageService.getReportsByOrderId(10L)).thenReturn(List.of());
        when(paymentService.getPaymentsByOrder(10L)).thenReturn(List.of());

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("order", order));
    }
}
