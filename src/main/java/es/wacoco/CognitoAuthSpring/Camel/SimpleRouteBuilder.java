package es.wacoco.CognitoAuthSpring.Camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SimpleRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:startRoute")
                .to("http://51.21.1.117/api/student")
                .log("Data retrieved from API: ${body}")
                .to("log:myLogger?level=INFO");
    }
}
