package com.gotokart.dto;

import java.util.List;

public record ImageBackfillResultDto(
        int total,
        int updated,
        int failed,
        List<String> failedNames
) {}
