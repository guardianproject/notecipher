/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package info.guardianproject.notepadbot;

import info.guardianproject.database.sqlcipher.SQLiteDatabase;
import info.guardianproject.database.sqlcipher.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATA = "odata";
    public static final String KEY_TYPE = "otype";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    
    private static NotesDbAdapter instance;
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + KEY_TITLE + " text not null, " + KEY_BODY + " text not null, " + KEY_DATA + " blob," + KEY_TYPE + " text);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 4;

    private Context mCtx;

   
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
          
           
            if (oldVersion == 2)
            {
            	db.execSQL("ALTER TABLE notes ADD " + KEY_DATA + " blog");
            	db.execSQL("ALTER TABLE notes ADD " + KEY_TYPE + " text");

            }
            
            if (newVersion == 3)
            {
            	db.execSQL("ALTER TABLE notes ADD " + KEY_TYPE + " text");
            }
            //need to migrate old notes here
            //  db.execSQL("DROP TABLE IF EXISTS notes");
            //onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    private NotesDbAdapter(Context ctx)
    {

        this.mCtx = ctx;
    	
    }
    
    
    public static synchronized NotesDbAdapter getInstance (Context ctx) {
    	
    	if (instance == null)
    	{
    		instance = new NotesDbAdapter(ctx);
    	}
    	
    	return instance;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open(String password) throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
       
        mDb = mDbHelper.getWritableDatabase(password);
        
        System.gc();
        return this;
    }
    
    public boolean isOpen ()
    {
    	if (mDb !=null)
    		return mDb.isOpen();
    	else
    		return false;
    }
    
    public void rekey (String password)
    {
    	mDb.execSQL("PRAGMA rekey = '" + password + "'");
    	System.gc();
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, byte[] data, String dataType) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        
        if (data != null)
        {
        	initialValues.put(KEY_DATA, data);
        	initialValues.put(KEY_TYPE, dataType);
        	
        }
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY, KEY_DATA, KEY_TYPE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, byte[] data, String dataType) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        
        if (data != null)
        {
        	args.put(KEY_DATA, data);
        	args.put(KEY_DATA, dataType);
        }
        
        
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
