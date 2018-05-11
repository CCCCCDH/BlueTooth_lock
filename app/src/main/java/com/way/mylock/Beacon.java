package com.way.mylock;

import java.io.Serializable;


public class Beacon implements Serializable {
    public String name ;
    public String address;

    public Beacon(String name,String address){
        this.name=name;
        this.address=address;
    }

    public String getName(){
        return name;
    }
    public String getAddress(){
        return address;
    }
}
