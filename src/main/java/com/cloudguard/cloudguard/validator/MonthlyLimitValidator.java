package com.cloudguard.cloudguard.validator;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyLimitValidator {

    public void validateMonthlyLimit(BigDecimal monthlyLimit){
        if(monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("월 예산은 0보다 커야합니다.");
        }
    }
}
