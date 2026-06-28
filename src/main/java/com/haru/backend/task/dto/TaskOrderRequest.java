package com.haru.backend.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record TaskOrderRequest(

        @NotEmpty(message = "순서 목록은 비어 있을 수 없습니다.")
        @Valid
        List<OrderItem> orders
) {

    public record OrderItem(

            @NotNull(message = "taskId는 필수입니다.")
            Long taskId,

            @NotNull(message = "displayOrder는 필수입니다.")
            @PositiveOrZero(message = "displayOrder는 0 이상이어야 합니다.")
            Integer displayOrder
    ) {
    }
}
