package com.example.demowithtests.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DocumentDto(
        Integer id,
        @NotNull
        String number,

        String uuid,

        LocalDateTime expireDate,

        LocalDateTime deleteDate,

        Integer idByUserBeforeDeleting,

        LocalDateTime dateAddDocument

) {
}