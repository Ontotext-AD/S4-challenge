package com.classicharmony.speechzilla.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.classicharmony.speechzilla.models.TheNote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by admin on 3/26/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "mSQLITE";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "notes_manager";
    // Table Names
    private static final String TABLE_Notes = "tablenotes";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_FULLTEXT = "full_text";
    private static final String KEY_Locations_list = "locations";
    private static final String KEY_Organizations_list = "organizations";
    private static final String KEY_Keywords = "keywords";
    private static final String KEY_CREATED_AT = "created_at";


    private static final String CREATE_TABLE_Notes = "CREATE TABLE "
            + TABLE_Notes + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FULLTEXT + " TEXT," + KEY_Locations_list
            + " TEXT," + KEY_Organizations_list + " TEXT," + KEY_Keywords + " TEXT," + KEY_CREATED_AT
            + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Notes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_Notes);
        // create new tables
        onCreate(db);
    }

    public void createNote(TheNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FULLTEXT, note.getFull_text());
        values.put(KEY_Locations_list, note.getLocation_list());
        values.put(KEY_Organizations_list, note.getOrganization_list());
        values.put(KEY_Keywords, note.getKeywords());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        db.insert(TABLE_Notes, null, values);

        Log.i("--- OK -----","---- note stored ------");

    }


    public List<TheNote> getAllNotes() {
        List<TheNote> notes = new ArrayList<TheNote>();
        String selectQuery = "SELECT  * FROM " + TABLE_Notes;
        Log.i(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                TheNote mNote = new TheNote();
                mNote.setFull_text(c.getString(c.getColumnIndex(KEY_FULLTEXT)));
                mNote.setKeywords(c.getString(c.getColumnIndex(KEY_Keywords)));
                mNote.setOrganization_list(c.getString(c.getColumnIndex(KEY_Organizations_list)));
                mNote.setLocation_list(c.getString(c.getColumnIndex(KEY_Locations_list)));
                mNote.setCreated_at(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                notes.add(mNote);
            } while (c.moveToNext());
        }

        return notes;
    }


    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_Notes, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void delete_ALL_Notes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_Notes);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
