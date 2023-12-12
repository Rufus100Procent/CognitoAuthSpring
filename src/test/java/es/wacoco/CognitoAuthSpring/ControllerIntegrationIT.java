//package es.wacoco.CognitoAuthSpring;
//
//import es.wacoco.CognitoAuthSpring.API.AuthController;
//import es.wacoco.CognitoAuthSpring.Service.CognitoAuthService;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(AuthController.class)
//@Tag("integration")
//public class ControllerIntegrationIT {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CognitoAuthService cognitoService;
//
//    @Test
//    void showRegistrationForm() throws Exception {
//        mockMvc.perform(get("/register"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("register"));
//    }
//
//    @Test
//    void register() throws Exception {
//        mockMvc.perform(post("/register")
//                        .param("username", "testUser")
//                        .param("password", "testPassword")
//                        .param("email", "test@example.com"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/confirm"));
//
//        verify(cognitoService).signUp("testUser", "testPassword", "test@example.com");
//    }
//
//    @Test
//    void showConfirmationForm() throws Exception {
//        mockMvc.perform(get("/confirm"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("confirm"));
//    }
//
//    @Test
//    void confirm() throws Exception {
//        mockMvc.perform(post("/confirm")
//                        .param("username", "testUser")
//                        .param("confirmationCode", "123456"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/login"));
//
//        verify(cognitoService).confirmSignUp("testUser", "123456");
//    }
//
//    @Test
//    void showLoginForm() throws Exception {
//        mockMvc.perform(get("/login"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("login"));
//    }
//
//
//    @Test
//    void deleteUser() throws Exception {
//        mockMvc.perform(delete("/deleteUser/{username}", "testUser"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("User deleted successfully"));
//
//        verify(cognitoService).deleteUser("testUser");
//    }
//
//}