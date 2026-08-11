package com.cloudguard.cloudguard.cost.controller;

import com.cloudguard.cloudguard.cost.dto.CostCreateRequest;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class CostController {

    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @PostMapping("/add-cost")
    public CostRecord addServiceCost(@RequestBody CostCreateRequest request){
        return costService.addServiceCost(
                request.getCloudService(),
                request.getCost(),
                request.getUsageDate()
        );
    }
}
