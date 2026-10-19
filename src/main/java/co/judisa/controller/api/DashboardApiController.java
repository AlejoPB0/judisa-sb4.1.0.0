package co.judisa.controller.api;

import co.judisa.dto.dashboard.DashboardResponse;
import co.judisa.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Panel")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Resumen del panel (RF-48, RF-50, RF-51)")
    public DashboardResponse resumen() {
        return dashboardService.resumen();
    }
}
