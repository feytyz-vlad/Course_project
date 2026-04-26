package ua.com.kisit.course_project.Controller.Web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class TestController {

    @GetMapping("/test/session")
    @ResponseBody
    public Map<String, Object> testSession(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", session.getAttribute("userId"));
        result.put("userEmail", session.getAttribute("userEmail"));
        result.put("userRole", session.getAttribute("userRole"));
        result.put("sessionId", session.getId());
        return result;
    }
}
