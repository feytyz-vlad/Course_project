package ua.com.kisit.course_project.Controller.Web;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
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

import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.AuditService;
import ua.com.kisit.course_project.Service.AuthenticationService;
import ua.com.kisit.course_project.Service.ClientService;
import ua.com.kisit.course_project.Service.CustomOAuth2UserService;
import ua.com.kisit.course_project.Service.CustomUserDetailsService;

@WebMvcTest(WebAuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ClientService clientService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private AuditService auditService;

    @Test
    void showLoginPage_Success() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void showRegisterPage_Success() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_PasswordsMismatch() throws Exception {
        mockMvc.perform(post("/auth/register")
                .param("email", "test@test.com")
                .param("password", "password123")
                .param("confirmPassword", "password456")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register"))
                .andExpect(flash().attribute(WebAuthController.ATTR_ERROR, "Паролі не співпадають"));

        verify(authService, never()).register(anyString(), anyString(), any(UserRole.class));
    }

    @Test
    void register_ShortPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                .param("email", "test@test.com")
                .param("password", "123")
                .param("confirmPassword", "123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register"))
                .andExpect(flash().attribute(WebAuthController.ATTR_ERROR, "Пароль мінімум 6 символів"));

        verify(authService, never()).register(anyString(), anyString(), any(UserRole.class));
    }

    @Test
    void register_Success() throws Exception {
        when(authService.register(eq("test@test.com"), eq("password123"), eq(UserRole.CLIENT)))
                .thenReturn(new User("test@test.com", "hash", UserRole.CLIENT));

        mockMvc.perform(post("/auth/register")
                .param("email", "test@test.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Реєстрація успішна! Тепер увійдіть."));

        verify(authService, times(1)).register("test@test.com", "password123", UserRole.CLIENT);
    }

    @Test
    void showProfile_RedirectsAnonymous() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showProfile_Authenticated_Success() throws Exception {
        User user = new User("user@test.com", "hash", UserRole.CLIENT);
        user.setUserId(10L);
        Client client = new Client(10L, "John", "Doe", "AB", "123456", "+380501234567", "LIC123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(clientService.getClientByUserId(10L)).thenReturn(Optional.of(client));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("client", client));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void completeProfile_PhoneValidationFailure() throws Exception {
        User user = new User("user@test.com", "hash", UserRole.CLIENT);
        user.setUserId(10L);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/register/complete")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("phone", "12345") // wrong phone pattern (must be +380XXXXXXXXX)
                .param("driverLicense", "LIC123")
                .param("rnokpp", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/complete"))
                .andExpect(flash().attribute(WebAuthController.ATTR_ERROR, "Телефон має бути у форматі +380XXXXXXXXX або +380-XX-XXX-XX-XX"));

        verify(clientService, never()).createClientProfile(any(Client.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void completeProfile_Success() throws Exception {
        User user = new User("user@test.com", "hash", UserRole.CLIENT);
        user.setUserId(10L);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/register/complete")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("phone", "+380501234567")
                .param("driverLicense", "LIC123")
                .param("rnokpp", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute(WebAuthController.ATTR_SUCCESS, "Профіль успішно збережено!"));

        verify(clientService, times(1)).createClientProfile(any(Client.class));
    }
}
