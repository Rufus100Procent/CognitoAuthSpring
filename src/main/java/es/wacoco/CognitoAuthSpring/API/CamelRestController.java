package es.wacoco.CognitoAuthSpring.API;

import es.wacoco.CognitoAuthSpring.Service.CamelServiceClass;
import es.wacoco.CognitoAuthSpring.Service.CognitoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CamelRestController {
    private static final Logger logger = LoggerFactory.getLogger(CamelRestController.class);

    private final CamelServiceClass camelServiceClass;
    private final CognitoAuthService cognitoAuthService;

    @Autowired
    public CamelRestController(CamelServiceClass camelServiceClass, CognitoAuthService cognitoAuthService) {
        this.camelServiceClass = camelServiceClass;
        this.cognitoAuthService = cognitoAuthService;
    }

    @GetMapping("/startRoute")
    public String startRoute(HttpServletRequest request) {
        String username = cognitoAuthService.getCurrentLoggedInUsername(request);
        return camelServiceClass.startRoute(username);
    }

}
