package com.vupico.ticket.domain;

import jakarta.validation.constraints.NotBlank;

public record ClassifyRequest(
        @NotBlank(message = "Text is required.")
        String text
) {}
