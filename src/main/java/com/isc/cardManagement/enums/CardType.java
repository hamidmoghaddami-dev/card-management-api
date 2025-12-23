package com.isc.cardManagement.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CardType {

    @JsonProperty("debit")
    DEBIT,
    @JsonProperty("credit")
    CREDIT
}
