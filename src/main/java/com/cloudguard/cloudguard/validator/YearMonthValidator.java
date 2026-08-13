package com.cloudguard.cloudguard.validator;

import java.time.YearMonth;

public class YearMonthValidator {

    public void validateYearMonth(YearMonth yearMonth){
        if(yearMonth == null){
            throw new IllegalArgumentException("연월은 필수입니다.");
        }
    }
}
