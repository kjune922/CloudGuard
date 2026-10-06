package com.cloudguard.cloudguard.cost.aws.controller;


import com.cloudguard.cloudguard.cost.aws.service.AwsCostImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * GOAL : AWS 비용을 수집해 DB에 저장, 갱신
 * HTTP 메서드 : POST
 * 경로 : /api/aws/costs/import
 * 호출하는 Service : AwsCostImportService
 * 정상 응답 : 204, 응답본문 X
 */

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
