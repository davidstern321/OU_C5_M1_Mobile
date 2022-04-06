package com.example.helloworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class DbAdapter {
    myDbHelper dbHelper;
    public DbAdapter(Context context)
    {
        dbHelper = new myDbHelper(context);
    }

    // Add a new user to the Users table
    public void usersInsert(String username, String password)
    {
        SQLiteDatabase dbb = dbHelper.getWritableDatabase(); // Get a database object
        ContentValues contentValues = new ContentValues();  // Store a set of values
        contentValues.put(myDbHelper.USERS_NAME, username); // Key and value
        contentValues.put(myDbHelper.USERS_PASSWORD, password); // Key and value
        dbb.insert(myDbHelper.USERS_TABLE_NAME, null , contentValues);  // Insert the selected values into the given table
    }

    public void usersInsertAWS(String username, String password) {
        Thread t = new Thread(new Runnable() { public void run() {
            URL awsEndpoint;
            HttpsURLConnection myConnection = null;
            try {
                String u = "https://8tlhfnfkg5.execute-api.us-east-2.amazonaws.com/Dev2?username="+username+"&password="+password;
                awsEndpoint = new URL(u);
                myConnection = (HttpsURLConnection) awsEndpoint.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setRequestProperty("Access-Control-Allow-Origin", "*");
                myConnection.setRequestProperty("Content-Type", "application/json");
                myConnection.setDoOutput(true);
                myConnection.getInputStream();
            } catch (MalformedURLException ex) { ex.printStackTrace();
            } catch (IOException ioe) { ioe.printStackTrace();
            } finally {
                myConnection.disconnect();
            }
        }});
        t.start();
    }

    // TODO: Add a new row to the Temperatures table
    // Add a new row to the Temperatures table
    public void temperaturesInsert(int desired, int outside, String time)
    {
        SQLiteDatabase dbb = dbHelper.getWritableDatabase(); // Get a database object
        ContentValues contentValues = new ContentValues();  // Store a set of values
        contentValues.put(myDbHelper.TEMP_DESIRED, desired); // Key and value
        contentValues.put(myDbHelper.TEMP_OUTSIDE, outside); // Key and value
        contentValues.put(myDbHelper.TEMP_TIMESTAMP, time); // Key and value
        dbb.insert(myDbHelper.TEMP_TABLE_NAME, null , contentValues);  // Insert the selected values into the given table
    }

    // Get all of the users in the Users table
    public String[] usersGetData()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get a database object
        String[] columns = {myDbHelper.USERS_ID, myDbHelper.USERS_NAME, myDbHelper.USERS_PASSWORD};
        Cursor cursor = db.query(myDbHelper.USERS_TABLE_NAME, columns,null,null,null,null,null);
        String[] result = new String[0];
        while (cursor.moveToNext())
        {
            int cid = cursor.getInt(cursor.getColumnIndex(myDbHelper.USERS_ID));
            String username = cursor.getString(cursor.getColumnIndex(myDbHelper.USERS_NAME));
            String password = cursor.getString(cursor.getColumnIndex(myDbHelper.USERS_PASSWORD));
            result = Arrays.copyOf(result, result.length + 1);
            result[result.length-1] = String.valueOf(cid) + "&" + username + "&" + password;
        }
        cursor.close();
        return result;
    }

    // If the current user exists return the password
    public String userExists(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get a database object
        String[] columns = {myDbHelper.USERS_ID, myDbHelper.USERS_NAME, myDbHelper.USERS_PASSWORD};
        Cursor cursor = db.query(myDbHelper.USERS_TABLE_NAME, columns,null,null,null,null,null);
        while (cursor.moveToNext())
        {
            String mUsername = cursor.getString(cursor.getColumnIndex(myDbHelper.USERS_NAME));

            if (mUsername.equals(username)) {
                String mPassword = cursor.getString(cursor.getColumnIndex(myDbHelper.USERS_PASSWORD));
                cursor.close(); // Close the reader
                return mPassword;   // Return the password from the database
            }
        }
        cursor.close(); // Close the reader
        return null;
    }

    // If the current user exists return the password
    public String userExistsAWS(String username) {
        final String[] pw = {null};
        Thread t = new Thread(new Runnable() { public void run() {
            URL awsEndpoint;
            HttpsURLConnection myConnection = null;
            try {
                String u = "https://r63km9v4ib.execute-api.us-east-2.amazonaws.com/Dev";
                awsEndpoint = new URL(u);
                myConnection = (HttpsURLConnection) awsEndpoint.openConnection();
                myConnection.setRequestMethod("GET");
                myConnection.setRequestProperty("Access-Control-Allow-Origin", "*");
                myConnection.setRequestProperty("Content-Type", "application/json");
                InputStream responseBody = myConnection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                JsonReader jsonReader = new JsonReader(responseBodyReader);

                jsonReader.beginObject(); // Start processing the JSON object
                while (jsonReader.hasNext()) { // Loop through all keys

                    String key = jsonReader.nextName(); // Fetch the next key
                    if (key.equals("result")) { // Check if desired key
                        // Fetch the value as a String
                        String value = jsonReader.nextString().replace("\"", "");

                        //value = value.substring(1, value.length() - 1);
                        String[] arrOfStr = value.split("], ");
                        for (String a : arrOfStr) {
                            String finalSplit = a;

                            finalSplit = finalSplit.replace("[", "");
                            finalSplit = finalSplit.replace("]", "");
                            String[] singleRecordSplit = finalSplit.split(", ");
                            if (singleRecordSplit != null && singleRecordSplit.length >= 2) {
                                if (singleRecordSplit[1].equals(username))
                                    pw[0] = singleRecordSplit[2];
                            }
                        }
                        break; // Break out of the loop
                    } else {
                        jsonReader.skipValue(); // Skip values of other keys
                    }
                }
            } catch (MalformedURLException ex) { ex.printStackTrace();
            } catch (IOException ioe) { ioe.printStackTrace();
            } finally {
                myConnection.disconnect();
            }
        }});
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            return null;
        }
        return pw[0];
    }

    // Delete a user based on their username
    // We are not going to use this, but good example of updating a value
    public int usersDelete(String username)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get a database object
        String[] whereArgs = {username};
        return  db.delete(myDbHelper.USERS_TABLE_NAME ,myDbHelper.USERS_NAME+" = ?",whereArgs);
    }

    // Update a user's username based on their ID
    // We are not going to use this, but good example of updating a value
    public int updateName(int userID , String newName)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Get a database object
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.USERS_NAME,newName);
        String[] whereArgs = {String.valueOf(userID)};
        return db.update(myDbHelper.USERS_TABLE_NAME,contentValues, myDbHelper.USERS_ID+" = ?",whereArgs);
    }

    static class myDbHelper extends SQLiteOpenHelper
    {
        private Context context;
        private static final String DATABASE_NAME = "TemperatureControlDatabase";    // Database Name - there's only one for the application
        private static final int DATABASE_VER = 1;    // Database Version

        // Users table setup
        // Columns: ID, Username, Password
        private static final String USERS_TABLE_NAME = "Users";   // Table Name
        private static final String USERS_ID="ID";     // Column I (Primary Key)
        private static final String USERS_NAME = "Username";    //Column II
        private static final String USERS_PASSWORD= "Password";    // Column III

        // create table Users (ID integer primary key autoincrement, Username varchar(255), Password varchar(255));
        private static final String CREATE_USERS_TABLE =
                "CREATE TABLE "+USERS_TABLE_NAME+" ("+USERS_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+USERS_NAME+" varchar(255) , "+USERS_PASSWORD+" varchar(225));";
        private static final String USERS_DROP_TABLE ="DROP TABLE IF EXISTS "+USERS_TABLE_NAME;

        // Temperatures table setup
        // Columns: ID, Timestamp, DesiredTemp, OutsideTemp
        // TODO: create variables for the Temperatures table setup
        private static final String TEMP_TABLE_NAME = "Temperatures";   // Table Name
        private static final String TEMP_ID="ID";     // Column I (Primary Key)
        private static final String TEMP_DESIRED = "DesiredTemp";    //Column II
        private static final String TEMP_OUTSIDE= "OutsideTemp";    // Column III
        private static final String TEMP_TIMESTAMP= "Timestamp";    // Column III

        // create table Users (ID integer primary key autoincrement, Username varchar(255), Password varchar(255));
        private static final String CREATE_TEMP_TABLE =
                "CREATE TABLE "+TEMP_TABLE_NAME+" ("+TEMP_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+TEMP_DESIRED+" integer , "+TEMP_OUTSIDE+" integer, " + TEMP_TIMESTAMP + " datetime);";
        private static final String TEMP_DROP_TABLE ="DROP TABLE IF EXISTS "+TEMP_TABLE_NAME;

        // Initialize the database from the context that created it
        // Create the database if it does not already exist with onCreate
        // Update the database if the version is different with onUpdate
        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VER);
            this.context=context;
        }

        // Called when the adapter is created
        public void onCreate(SQLiteDatabase db) {
            try {
                // Create the Users table
                db.execSQL(CREATE_USERS_TABLE);

                // Try to create the Temperatures table
                db.execSQL(CREATE_TEMP_TABLE);
                // TODO: Create the Temperatures table
            } catch (Exception e) {
                Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Message.message(context,"OnUpgrade");
                // Drop the Users table and then recreate it
                db.execSQL(USERS_DROP_TABLE);
                db.execSQL(TEMP_DROP_TABLE);
                onCreate(db);

                // Drop the Temperatures table and then recreate it
                // TODO: Drop the Temperatures table and then recreate it
            } catch (Exception e) {
                Message.message(context,""+e);
            }
        }
    }
}