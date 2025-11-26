package pe.edu.upc.bonotech.shared.infrastructure.documentation.openapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SwaggerInfoListener {

    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    private boolean alreadyPrinted = false;

    @EventListener(ServletWebServerInitializedEvent.class)
    public void printSwaggerUrls(ServletWebServerInitializedEvent event) {
        if (alreadyPrinted) {
            return;
        }
        alreadyPrinted = true;
        
        int port = event.getWebServer().getPort();
        String baseUrl = "http://localhost:" + port + contextPath;
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Swagger UI está disponible en:");
        System.out.println("   " + baseUrl + "/swagger-ui.html");
        System.out.println("   " + baseUrl + "/swagger-ui/index.html");
        System.out.println("\n📚 API Documentation (JSON):");
        System.out.println("   " + baseUrl + "/v3/api-docs");
        System.out.println("=".repeat(80) + "\n");
    }
}

