package com.isc.cardManagement.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum AccountType {
    CURRENT,
    SAVINGS,
    LONG_TERM
}

