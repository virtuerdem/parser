/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

/**
 *
 * @author TTGParserTeam©
 */
public enum MotorolaTimePeriods {

    time0("00:00"), time1("01:00"), time2("02:00"), time3("03:00"),
    time4("04:00"), time5("05:00"), time6("06:00"), time7("07:00"),
    time8("08:00"), time9("09:00"), time10("10:00"), time11("11:00"),
    time12("12:00"), time13("13:00"), time14("14:00"), time15("15:00"),
    time16("16:00"), time17("17:00"), time18("18:00"), time19("19:00"),
    time20("20:00"), time21("21:00"), time22("22:00"), time23("23:00");

    private final String Times;
    private final TimePeriodProp timePeriodProp;
    private String tableName;
    private String omcName;
    private String Date;

    MotorolaTimePeriods(String time) {
        this.Times = time;
        timePeriodProp = new TimePeriodProp();
    }

    public void resetTimePeriodContext() {
        tableName = null;
        omcName = null;
        Date = null;
        timePeriodProp.setCount(0);
    }

    public String getTimes() {
        return Times;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOmcName() {
        return omcName;
    }

    public void setOmcName(String omcName) {
        this.omcName = omcName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public TimePeriodProp getTimePeriodProp() {
        return timePeriodProp;
    }

    class TimePeriodProp {

        private int count = 0;

        public TimePeriodProp() {
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void count() {
            count = count + 1;
        }

        public int getCount() {
            return count;
        }

    }

}