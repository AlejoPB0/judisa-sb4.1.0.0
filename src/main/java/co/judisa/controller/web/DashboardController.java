package co.judisa.controller.web;

import co.judisa.dto.corte.CorteFiltro;
import co.judisa.service.CorteService;
import co.judisa.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** RF-48 panel principal tras el login, RF-49 accesos directos, RF-50/51 resumen. */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final CorteService corteService;

    public DashboardController(DashboardService dashboardService, CorteService corteService) {
        this.dashboardService = dashboardService;
        this.corteService = corteService;
    }

    @GetMapping("/")
    public String panel(Model model) {
        model.addAttribute("resumen", dashboardService.resumen());
        model.addAttribute("ultimosCortes", corteService.listar(new CorteFiltro(null, null, null, null),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fecha", "id"))).content());
        return "dashboard/index";
    }
}
