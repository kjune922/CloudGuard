package com.cloudguard.cloudguard.cost.aws.exception;

public class InvalidCostPeriodException extends IllegalArgumentException{

    public InvalidCostPeriodException(String message) {
        super(message);
    }
}
