package es.wacoco.CognitoAuthSpring.API;

import es.wacoco.CognitoAuthSpring.Service.CognitoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    private final CognitoAuthService cognitoAuthService;

    @Autowired
    public RestController(CognitoAuthService cognitoAuthService) {
        this.cognitoAuthService = cognitoAuthService;
    }
    @GetMapping("/isadmin")
    public ResponseEntity<Boolean> checkAdminStatus(@RequestParam String username) {
        boolean isAdmin = cognitoAuthService.isAdmin(username);
        return new ResponseEntity<>(isAdmin, HttpStatus.OK);
    }
}
