package com.huntloc.eocmessenger;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dmoran on 7/10/2017.
 */

public class Client {
    private String Id;
    private String Name;
    private String Token;
    private boolean IsAdmin;


    private HashMap<String, Boolean> Groups;
    public Client() {
    }
    public Client(String id, String name, String token, boolean isAdmin, HashMap<String, Boolean> groups) {
        Id = id;
        Name = name;
        Token = token;
        IsAdmin = isAdmin;
        Groups = groups;
    }
    public HashMap<String, Boolean> getGroups() {
        return Groups;
    }
    public void setGroups(HashMap<String, Boolean> groups) {
        Groups = groups;
    }
    public boolean isAdmin() {
        return IsAdmin;
    }
    public void setAdmin(boolean admin) {
        IsAdmin = admin;
    }
    public String getId() {
        return Id;
    }
    public void setId(String id) {
        Id = id;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public String getToken() {
        return Token;
    }
    public void setToken(String token) {
        Token = token;
    }
    @Override
    public String toString() {
        return "Client{" +
                "Id='" + Id + '\'' +
                ", Name='" + Name + '\'' +
                ", Token='" + Token + '\'' +
                ", IsAdmin=" + IsAdmin +
                ", Groups=" + Groups.toString() +
                '}';
    }
}
