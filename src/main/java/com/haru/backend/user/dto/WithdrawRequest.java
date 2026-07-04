package com.haru.backend.user.dto;

import java.util.List;

public record WithdrawRequest(
        List<String> reasons,
        String etcReason
) {
}
