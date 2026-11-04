package rdhxb.raportservice.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import rdhxb.common.base.Source;

import rdhxb.raportservice.model.SourceStatus;

public record SourceRaportDto(

        Source source,
        SourceStatus status,
        String errorMessage,
        // in db we saved payload as String, we want to pass the
        // payload as JSON so we put jsonRawValue annotation to convert it
        @JsonRawValue
        String payload

){}
