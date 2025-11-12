package com.pcanabarro.dto;

import lombok.Data;

@Data
public class CDCValue {
    private Object before;
    private Object after;
    private Source source;
    private Object transaction;
    private String op;
    private long ts_ms;
    private long ts_us;
    private long ts_ns;
}