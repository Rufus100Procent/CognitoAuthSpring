package es.wacoco.CognitoAuthSpring.Service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CamelServiceClass {

    private final CamelContext camelContext;

    @Autowired
    public CamelServiceClass(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public String startRoute(){
        try {
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
