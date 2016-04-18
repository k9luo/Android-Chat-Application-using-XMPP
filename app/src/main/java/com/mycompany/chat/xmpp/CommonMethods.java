package com.mycompany.chat.xmpp;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonMethods {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
    private SQLiteDatabase mydb;
    private String INSERT;
    public static String DBNAME="chat.db";
    private SQLiteStatement insertStmt;
    Context context;
    public CommonMethods(Context context) {
        this.context=context;
    }
    public static String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
    private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public void createTable(String tablename) {
        String tblname="'"+tablename+"'";
        try {
            mydb = context.openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
            mydb.execSQL("CREATE TABLE IF  NOT EXISTS " + tblname + " (ID INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, msg TEXT, who TEXT, type TEXT);");
            mydb.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error in creating table", Toast.LENGTH_LONG);
        }
    }
    //INSERT INTO 07665078550 VALUES ( null, ?, ?, ?, ?)
    public void insertIntoTable(String tablename, String s, String r, String m, String w,String datatype) {
        String tblname="'"+tablename+"'";
        try {
            mydb = context.openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
            String sql="INSERT INTO "+tblname +" VALUES ( null, ?, ?, ?, ?, ?)";
            mydb.execSQL(sql, new String[]{s,r,m,w,datatype});

            mydb.close();
            Log.i("DB", "INSERTED");
        } catch (Exception e) {
            Log.i("DBERROR",e.toString());
        }
    }
}