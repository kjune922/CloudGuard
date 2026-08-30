package com.cloudguard.cloudguard.cost.domain;

public enum CostSource {
    MANUAL, // 기존 비용 등록 API로 직접 입력한 비용
    AWS_COST_EXPLORER // AWS에서 수집한 비용
}
