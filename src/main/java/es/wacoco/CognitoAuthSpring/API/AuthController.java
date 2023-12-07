package es.wacoco.CognitoAuthSpring.API;

import es.wacoco.CognitoAuthSpring.Service.CognitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListGroupsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;

@Controller
//http://localhost:8080/swagger-ui/index.html#/
//http://localhost:8080/v3/api-docs
public class AuthController {

    private final CognitoService cognitoService;

    @Autowired
    public AuthController(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @Operation(summary = "Show registration form")
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @Operation(summary = "Register user")
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email
    ) {
        cognitoService.signUp(username, password, email);
        return "redirect:/confirm";
    }

    @Operation(summary = "Show confirmation form")
    @GetMapping("/confirm")
    public String showConfirmationForm() {
        return "confirm";
    }

    @Operation(
            summary = "Confirm user registration",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Confirmation successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid confirmation code", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/confirm")
    public String confirm(
            @RequestParam String username,
            @RequestParam String confirmationCode
    ) {
        cognitoService.confirmSignUp(username, confirmationCode);
        return "redirect:/login";
    }

    @Operation(summary = "Show login form")
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @Operation(
            summary = "User login",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            Model model
    ) {
        AuthenticationResultType authResult = cognitoService.signIn(username, password);
        model.addAttribute("accessToken", authResult.accessToken());
        return "redirect:/dashboard";
    }

    @Operation(summary = "Show dashboard")
    @GetMapping("/dashboard")
    public String dashBoard(Model model) {
        ListUsersResponse listUsersResponse = cognitoService.listUsers();
        model.addAttribute("users", listUsersResponse.users());

        ListGroupsResponse groupsResponse = cognitoService.listAllGroups();

        // Add the list of group names to the model
        model.addAttribute("groups", groupsResponse.groups());
        return "dashboard";
    }

    @Operation(summary = "Add user to group")
    @PostMapping("/addUserToGroup")
    @ResponseBody
    public void addUserToGroup(@RequestParam String username, @RequestParam String groupName) {
        cognitoService.addUserToGroup(username, groupName);
    }
    @Operation(
            summary = "Delete user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @DeleteMapping("/deleteUser/{username}")
    @ResponseBody
    public String deleteUser(@PathVariable String username) {
        try {
            cognitoService.deleteUser(username);
            return "User deleted successfully";
        } catch (Exception e) {
            return "Error deleting user: " + e.getMessage();
        }
    }


    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String username, @RequestParam String newPassword, @RequestParam String previousPassword) {
        cognitoService.changePassword(username, previousPassword);
        return "redirect:/dashboard";
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}
