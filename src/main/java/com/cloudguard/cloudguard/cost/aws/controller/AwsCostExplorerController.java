package com.cloudguard.cloudguard.cost.aws.controller;


import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.service.AwsCostExplorerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/aws/costs")
public class AwsCostExplorerController {

    private final AwsCostExplorerService awsCostExplorerService;

    public AwsCostExplorerController(AwsCostExplorerService awsCostExplorerService) {
        this.awsCostExplorerService = awsCostExplorerService;
    }

    @GetMapping
    public List<AwsServiceCost> getServiceCosts(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return awsCostExplorerService.getServiceCosts(startDate,endDate);
    }


}
