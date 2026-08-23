package com.cloudguard.cloudguard.cost.controller;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.dto.CostCreateRequest;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.dto.MonthlyCostBreakdownResponse;
import com.cloudguard.cloudguard.cost.dto.MonthlyCostResponse;
import com.cloudguard.cloudguard.cost.dto.MonthlyServiceCostResponse;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/costs")
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

    @GetMapping("/monthly")
    public MonthlyCostResponse getMonthlyCost(
            @RequestParam("yearMonth")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
            ) {
        BigDecimal totalCost = costService.calculateMonthlyCost(yearMonth);

        return new MonthlyCostResponse(yearMonth,totalCost);
    }

    @GetMapping("/monthly/by-service")
    public MonthlyServiceCostResponse getMonthlyCostByService(
            @RequestParam("yearMonth")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam("service") CloudService service
    ) {
        BigDecimal totalCost =
                costService.calculateMonthlyCostByService(
                        yearMonth,
                        service
                );

        return new MonthlyServiceCostResponse(
                yearMonth,
                service,
                totalCost
        );
    }

    @GetMapping("/monthly/breakdown")
    public MonthlyCostBreakdownResponse getMonthlyCostBreakdown(
            @RequestParam("yearMonth")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ){
        Map<CloudService, BigDecimal> serviceCosts =
                costService.calculateMonthlyCostBreakdown(yearMonth);

        BigDecimal totalCost = serviceCosts.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyCostBreakdownResponse(
                yearMonth,
                totalCost,
                serviceCosts
        );
    }
}
