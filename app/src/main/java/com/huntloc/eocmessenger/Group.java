package com.huntloc.eocmessenger;

/**
 * Created by dmoran on 7/19/2017.
 */

public class Group {
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Group(String name, String message) {
        Name = name;
        Message = message;
    }

    private String Name;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    private String Message;
    @Override
    public String toString() {
        return Name;
    }
}
