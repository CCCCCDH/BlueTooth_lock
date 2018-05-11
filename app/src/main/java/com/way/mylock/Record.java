package com.way.mylock;

public class Record {
    private String name;
    private String time;
    private String visitor;

    public Record(String visitor, String name, String time) {
        this.visitor = visitor;
        this.name = name;
        this.time = time;
    }
    public String getName(){
        return name;
    }
    public String getVisitor(){
        return visitor;
    }
    public String getTime(){
        return  time;
    }
}