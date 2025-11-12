package com.pcanabarro.dto;

import lombok.Data;

@Data
public class Source {
    private String version;
    private String connector;
    private String name;
    private long ts_ms;
    private String snapshot;
    private String db;
    private Object sequence;
    private long ts_us;
    private long ts_ns;
    private String table;
    private int server_id;
    private Object gtid;
    private String file;
    private int pos;
    private int row;
    private int thread;
    private Object query;
}