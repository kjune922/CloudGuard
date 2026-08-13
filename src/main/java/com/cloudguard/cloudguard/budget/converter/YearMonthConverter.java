package com.cloudguard.cloudguard.budget.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.time.YearMonth;

@Convert
public class YearMonthConverter implements AttributeConverter<YearMonth,String> {


    // DB에 저장할 때
    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        if(yearMonth == null){
            return null;
        }
        return yearMonth.toString();
    }


    // DB에서 조회할 때
    @Override
    public YearMonth convertToEntityAttribute(String value) {
        if(value == null){
            return null;
        }
        return YearMonth.parse(value);
    }
}
