package com.dataexpo.dataexpozkgate.model;

public class PassRecord {
    private String name;
    private String printtime;
    private String company;
    private String rule;
    private String code;
    private String expoid;

    public PassRecord(String name, String printtime, String company, String rule, String code, String expoid) {
        this.name = name;
        this.printtime = printtime;
        this.company = company;
        this.rule = rule;
        this.code = code;
        this.expoid = expoid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrinttime() {
        return printtime;
    }

    public void setPrinttime(String printtime) {
        this.printtime = printtime;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExpoid() {
        return expoid;
    }

    public void setExpoid(String expoid) {
        this.expoid = expoid;
    }
}
