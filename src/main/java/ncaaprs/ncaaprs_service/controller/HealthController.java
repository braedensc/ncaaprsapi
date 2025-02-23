package ncaaprs.ncaaprs_service.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/ready")
    public String readinessCheck() {
        // Add any readiness logic here if necessary
        return "READY";
    }
}
