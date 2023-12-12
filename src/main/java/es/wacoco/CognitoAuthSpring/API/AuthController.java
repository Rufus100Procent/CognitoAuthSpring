package es.wacoco.CognitoAuthSpring.API;

import es.wacoco.CognitoAuthSpring.Service.CognitoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListGroupsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final CognitoAuthService cognitoService;

    @Autowired
    public AuthController(CognitoAuthService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email
    ) {
        try {
            cognitoService.signUp(username, password, email);
            return "redirect:/confirm";
        } catch (CognitoAuthService.CognitoServiceException e) {
            return "redirect:/register?error=" + e.getMessage();
        }
    }

    @GetMapping("/confirm")
    public String showConfirmationForm() {
        return "confirm";
    }

    @PostMapping("/confirm")
    public String confirm(
            @RequestParam String username,
            @RequestParam String confirmationCode
    ) {
        try {
            cognitoService.confirmSignUp(username, confirmationCode);
            return "redirect:/login";
        } catch (CognitoAuthService.CognitoServiceException e) {
            return "redirect:/confirm?error=" + e.getMessage();
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            Model model
    ) {
        try {
            AuthenticationResultType authResult = cognitoService.signIn(username, password, request);
            model.addAttribute("accessToken", authResult.accessToken());
            return "redirect:/dashboard";
        } catch (CognitoAuthService.CognitoServiceException e) {
            logger.error("Cognito Service Exception: {}", e.getMessage());

            // Optionally, you can rethrow the exception if you want it to be handled elsewhere
            throw e;
        }
    }

    @GetMapping("/dashboard")
    public String dashBoard(HttpServletRequest request, Model model) {
        try {
            ListUsersResponse listUsersResponse = cognitoService.listUsers(request);
            model.addAttribute("users", listUsersResponse.users());

            ListGroupsResponse groupsResponse = cognitoService.listAllGroups();
            model.addAttribute("groups", groupsResponse.groups());

            return "dashboard";
        } catch (CognitoAuthService.CognitoServiceException e) {
            return "redirect:/login?error=" + e.getMessage();
        }
    }

    @PostMapping("/addUserToGroup")
    @ResponseBody
    public void addUserToGroup(@RequestParam String username, @RequestParam String groupName) {
        cognitoService.addUserToGroup(username, groupName);
    }

    @DeleteMapping("/deleteUser/{username}")
    @ResponseBody
    public String deleteUser(@PathVariable String username, HttpServletRequest request) {
        try {
            cognitoService.deleteUser(username, request);
            return "User deleted successfully";
        } catch (CognitoAuthService.CognitoServiceException e) {
            return "Error deleting user: " + e.getMessage();
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String username, @RequestParam String newPassword, @RequestParam String previousPassword) {
        try {
            cognitoService.changePassword(username, newPassword, previousPassword);
            return "redirect:/dashboard";
        } catch (CognitoAuthService.CognitoServiceException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    }
}
