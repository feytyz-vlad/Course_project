package ua.com.kisit.course_project.Controller.Web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.AuthenticationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(WebAuthController.class)
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @Mock
    private User mockUser;

    @Test
    void loginSuccessRedirectsToHome() throws Exception {
        when(authService.login("user@example.com", "password")).thenReturn("token123");
        User u = mock(User.class);
        when(u.getUserId()).thenReturn(1L);
        when(u.getEmail()).thenReturn("user@example.com");
        when(u.getRole()).thenReturn(UserRole.CLIENT);
        when(authService.validateSession("token123")).thenReturn(Optional.of(u));

        mockMvc.perform(post("/auth/login")
                .param("email", "user@example.com")
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(authService).login("user@example.com", "password");
        verify(authService).validateSession("token123");
    }

    @Test
    void loginFailureRedirectsBackToLogin() throws Exception {
        when(authService.login(anyString(), anyString())).thenThrow(new RuntimeException("bad creds"));

        mockMvc.perform(post("/auth/login")
                .param("email", "wrong@example.com")
                .param("password", "bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).login("wrong@example.com", "bad");
    }
}
