package es.wacoco.CognitoAuthSpring.API;

import es.wacoco.CognitoAuthSpring.Service.CamelServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CamelRestController {
    private final CamelServiceClass camelServiceClass;

    @Autowired
    public CamelRestController(CamelServiceClass camelServiceClass) {
        this.camelServiceClass = camelServiceClass;
    }

    @GetMapping("/startRoute")
    public String startRoute() {
        return camelServiceClass.startRoute();
    }
}
