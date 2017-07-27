package com.huntloc.eocmessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EOCDB";

    private static final String TABLE_CLIENT = "Client";
    private static final String KEY_GUID = "guid";
    private static final String KEY_NAME = "name";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ISADMIN = "isadmin";

    private static final String TABLE_CLIENT_GROUP = "ClientGroups";
    private static final String KEY_RECEIVED = "received";

    private static final String TABLE_GROUP = "Groups";
    private static final String KEY_GROUP_NAME = "name";
    private static final String KEY_GROUP_MESSAGE = "message";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_CLIENT_TABLE = "CREATE TABLE Client ( guid TEXT PRIMARY KEY, name TEXT, token TEXT, isadmin INTEGER)";
        db.execSQL(CREATE_CLIENT_TABLE);

        String CREATE_GROUP_TABLE = "CREATE TABLE Groups ( name TEXT, message TEXT)";
        db.execSQL(CREATE_GROUP_TABLE);

        String CREATE_CLIENT_GROUP_TABLE = "CREATE TABLE ClientGroups ( guid TEXT, name TEXT, received INTEGER )";
        db.execSQL(CREATE_CLIENT_GROUP_TABLE);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT_GROUP);
        this.onCreate(db);
    }

    public void addClient(Client client) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GUID, client.getId());
        values.put(KEY_NAME, client.getName());
        values.put(KEY_TOKEN, client.getToken());
        values.put(KEY_ISADMIN, client.isAdmin() ? 1 : 0);
        db.insert(TABLE_CLIENT, null, values);
        db.close();
        deleteClientGroups(client.getId());
        for (String key: client.getGroups().keySet()) {
            addClientGroup(client.getId(), key , client.getGroups().get(key)?1:0);
        }
    }

    public void addClientGroup(String id, String group, int received) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GUID, id);
        values.put(KEY_NAME, group);
        values.put(KEY_RECEIVED, received);
        db.insert(TABLE_CLIENT_GROUP, null, values);
        db.close();
    }

    public void deleteClientGroups(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CLIENT_GROUP + " where " + KEY_GUID + " = '" + id + "'");
        db.close();
    }

    public HashMap<String,Boolean> getClientGroups(String id) {
        HashMap<String,Boolean> groups = new HashMap<String,Boolean>();
        String query = "SELECT  * FROM " + TABLE_CLIENT_GROUP + " where " + KEY_GUID + " = '" + id + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                groups.put(cursor.getString(1),cursor.getInt(2) == 1 ? true : false);
            } while (cursor.moveToNext());
        }
        db.close();
        return groups;
    }

    public void deleteClients() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CLIENT);
        db.close();
    }

    public List<Client> getClientsByGroup(String name) {
        List<Client> toReturn = new LinkedList<>();
        String query = "SELECT  Client.* FROM Client, ClientGroups where ClientGroups.guid = Client.guid and ClientGroups.name = '"+name+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Client client = null;
        if (cursor.moveToFirst()) {
            do {
                client = new Client(cursor.getString(0),cursor.getString(1),cursor.getString(2), cursor.getInt(3) == 1 ? true : false, getClientGroups(cursor.getString(0)));
                toReturn.add(client);
            } while (cursor.moveToNext());
        }
        db.close();
        return toReturn;
    }
    public Client getClient(String id) {
        Client client = null;
        String query = "SELECT  * FROM " + TABLE_CLIENT + " where " + KEY_GUID + " = '" + id + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            client = new Client(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3) == 1 ? true : false, getClientGroups(cursor.getString(0)));
        }
        db.close();
        return client;
    }
    public void addGroup(Group group) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_NAME, group.getName());
        values.put(KEY_GROUP_MESSAGE, group.getMessage());
        db.insert(TABLE_GROUP, null, values);
        db.close();
    }

    public void deleteGroups() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_GROUP);
        db.close();
    }

    public List<Group> getGroups() {
        List<Group> toReturn = new LinkedList<>();
        String query = "SELECT * FROM " + TABLE_GROUP;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Group group;
        if (cursor.moveToFirst()) {
            do {
                group = new Group(cursor.getString(0), cursor.getString(1));
                toReturn.add(group);
            } while (cursor.moveToNext());
        }
        db.close();
        return toReturn;
    }
}
