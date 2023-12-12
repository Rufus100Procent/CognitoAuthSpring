package es.wacoco.CognitoAuthSpring.Service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CamelServiceClass {
    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);

    private final CamelContext camelContext;
    private final CognitoAuthService cognitoAuthService;
    @Autowired
    public CamelServiceClass(CamelContext camelContext, CognitoAuthService cognitoAuthService) {
        this.camelContext = camelContext;
        this.cognitoAuthService = cognitoAuthService;
    }

    public String startRoute(String username) {
        try {
            if (!cognitoAuthService.isAdmin(username)) {
                logger.warn("Unauthorized access attempt by user: {}", username);
                return "User does not have permission to access this route.";
            }

            // Trigger the Camel route using ProducerTemplate
            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            // Sending a message to direct:startRoute to initiate the route
            String response = producerTemplate.requestBody("direct:startRoute", null, String.class);

            return "Camel route processing initiated! Response: " + response + getFormattedTimestamp();
        } catch (Exception e) {
            // Log the exception or handle it in a way that provides more details
            e.printStackTrace(); // For logging to console

            return "Error occurred: " + e.getMessage();
        }
    }

    private String getFormattedTimestamp() {
        // Get the current timestamp using a custom formatter without milliseconds
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(customFormatter);
    }
}
