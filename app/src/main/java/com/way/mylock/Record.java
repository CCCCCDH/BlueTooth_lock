package com.way.mylock;

/**
 * Created by wise on 2015/11/8.
 */
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