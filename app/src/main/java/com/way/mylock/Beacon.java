package com.way.mylock;

import java.io.Serializable;

/**
 * Created by wise on 2015/10/8.
 */
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
