/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.database.sqlite;

/* we need some of this stuff that is @hide hidden in the SDK, 
 * so this is just a shell for Eclipse to be happy */

import android.database.Cursor;
import android.database.sqlite.SQLCipherDatabase.CursorFactory;

/**
 * A cursor driver that uses the given query directly.
 * 
 * @hide
 */
public class SQLiteDirectCursorDriver implements SQLiteCursorDriver {
    private String mEditTable; 
    private SQLCipherDatabase mDatabase;
    private Cursor mCursor;
    private String mSql;
    private SQLCipherQuery mQuery;

    public SQLiteDirectCursorDriver(SQLCipherDatabase db, String sql, String editTable) {
        mDatabase = db;
        mEditTable = editTable;
        mSql = sql;
    }

    public Cursor query(CursorFactory factory, String[] selectionArgs) {
    	// deleted contents to eliminate errors
    	mCursor = new SQLiteCursor(mDatabase, this, mEditTable, null);
    	return mCursor;
    }

    public void cursorClosed() {
        mCursor = null;
    }

    public void setBindArguments(String[] bindArgs) {
        final int numArgs = bindArgs.length;
        for (int i = 0; i < numArgs; i++) {
            mQuery.bindString(i + 1, bindArgs[i]);
        }
    }

    public void cursorDeactivated() {
        // Do nothing
    }

    public void cursorRequeried(Cursor cursor) {
        // Do nothing
    }

    @Override
    public String toString() {
        return "SQLiteDirectCursorDriver: " + mSql;
    }
}
