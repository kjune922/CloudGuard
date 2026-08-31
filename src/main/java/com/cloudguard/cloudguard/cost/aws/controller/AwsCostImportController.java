package com.cloudguard.cloudguard.cost.aws.controller;


import com.cloudguard.cloudguard.cost.aws.service.AwsCostImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/aws/costs")
public class AwsCostImportController {

    private final AwsCostImportService awsCostImportService;

    public AwsCostImportController(AwsCostImportService awsCostImportService){
        this.awsCostImportService = awsCostImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importCosts(
            @RequestParam ("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
            ) {
        awsCostImportService.importCosts(startDate,endDate);

        return ResponseEntity.noContent().build();
    }
}
