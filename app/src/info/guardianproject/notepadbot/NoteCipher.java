/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")savedInstanceState;
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

package info.guardianproject.notepadbot;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class NoteCipher extends SherlockActivity implements ICacheWordSubscriber {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int REKEY_ID = Menu.FIRST + 2;
    private static final int SHARE_ID = Menu.FIRST + 3;
    private static final int VIEW_ID = Menu.FIRST + 4;
    private static final int LOCK_ID = Menu.FIRST + 5;
    private static final int CHANGE_TIMEOUT = Menu.FIRST + 6;

    public static final String TAG = "notecipher";

    private NotesDbAdapter mDbHelper;

    private Uri dataStream;

    private final static int MAX_SIZE = 1000000;

    private CacheWordActivityHandler mCacheWord;
    
    private ListView notesListView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
                dataStream = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
            } else
                dataStream = getIntent().getData();

        }
        
        SQLiteDatabase.loadLibs(this);
        setContentView(R.layout.notes_list);
        notesListView = (ListView) findViewById(R.id.notesListView);
        notesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> ad, View v, int position,
					long id) {
				Intent i = new Intent(getApplication(), NoteEdit.class);
	            i.putExtra(NotesDbAdapter.KEY_ROWID, id);
	            startActivityForResult(i, ACTIVITY_EDIT);
			}
        }); 
        registerForContextMenu(notesListView);
        mCacheWord = new CacheWordActivityHandler(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCacheWord.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCacheWord.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        findViewById(R.id.listlayout).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mDbHelper != null && mDbHelper.isOpen())
                    createNote();

                return false;
            }
        });
    }

    private void closeDatabase() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    private void unlockDatabase() {
        mDbHelper = new NotesDbAdapter(mCacheWord, this);
        try {

            mDbHelper.open();

            if (dataStream != null)
                importDataStream();
            else
                fillData();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.err_pass), Toast.LENGTH_LONG).show();
        }
    }

    private void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list
        // (only TITLE)
        String[] from = new String[] {
            NotesDbAdapter.KEY_TITLE
        };

        // and an array of the fields we want to bind those fields to (in this
        // case just text1)
        int[] to = new int[] {
            R.id.text1
        };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to,  SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );
        notesListView.setAdapter(notes);

        if (notes.isEmpty()) {
            Toast.makeText(this, R.string.on_start, Toast.LENGTH_LONG).show();
            ((TextView)findViewById(R.id.emptytext)).setText(R.string.no_notes);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        // menu.add(0, REKEY_ID, 0, R.string.menu_rekey);
        menu.add(0, LOCK_ID, 0, R.string.menu_lock);
        menu.add(0, CHANGE_TIMEOUT, 0, R.string.menu_timeout);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
            case REKEY_ID:
                return true;
            case LOCK_ID:
                if (!mCacheWord.isLocked())
                    mCacheWord.manuallyLock();
                return true;
            case CHANGE_TIMEOUT:
                changeTimeoutPrompt();
                return true;

        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // menu.add(0, VIEW_ID, 0, R.string.menu_view);
        menu.add(0, SHARE_ID, 0, R.string.menu_share);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);

    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info;
        
        switch (item.getItemId()) {
            case DELETE_ID:
                info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
            case SHARE_ID:
                info = (AdapterContextMenuInfo) item.getMenuInfo();
                shareEntry(info.id);

                return true;
            case VIEW_ID:
                info = (AdapterContextMenuInfo) item.getMenuInfo();
                viewEntry(info.id);

                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void shareEntry(long id) {
        Cursor note = mDbHelper.fetchNote(id);
        // If you do startManagingCursor(note) here it crashes when the user
        // returns to the app after sharing the text he wants
        //startManagingCursor(note);

        byte[] blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
        String title = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
        String mimeType = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE));

        if (mimeType == null)
            mimeType = "text/plain";

        if (blob != null) {
            try {
                NoteUtils.shareData(this, title, mimeType, blob);
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.err_export, e.getMessage()), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            String body = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
            NoteUtils.shareText(this, body);
        }

        note.close();
    }

    private void viewEntry(long id) {
        Cursor note = mDbHelper.fetchNote(id);
        startManagingCursor(note);

        byte[] blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
        String mimeType = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE));

        if (mimeType == null)
            mimeType = "text/plain";

        if (blob != null) {
            String title = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));

            NoteUtils.savePublicFile(this, title, mimeType, blob);

        }

        note.close();
    }

    private void createNote() {
        if (mCacheWord.isLocked())
            return;

        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /*
     * Called after the return from creating a new note (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        mDbHelper = new NotesDbAdapter(mCacheWord, this);

        fillData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeDatabase();
        NoteUtils.cleanupTmp(this);
    }

    private void importDataStream() {
        if (mCacheWord.isLocked())
            return;

        try {
            ContentResolver cr = getContentResolver();
            InputStream is = cr.openInputStream(dataStream);

            String mimeType = cr.getType(dataStream);

            byte[] data = NoteUtils.readBytesAndClose(is);

            if (data.length > MAX_SIZE){
                Toast.makeText(this, R.string.err_size, Toast.LENGTH_LONG).show();

            }
            else {
                String title = dataStream.getLastPathSegment();
                String body = dataStream.getPath();

                new NotesDbAdapter(mCacheWord, this).createNote(title, body, data, mimeType);

                Toast.makeText(this, getString(R.string.on_import, title), Toast.LENGTH_LONG).show();

                // handleDelete();

                data = null;
                dataStream = null;
                title = null;
                body = null;

                System.gc();

                fillData();
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);

        } catch (OutOfMemoryError e) {
            Toast.makeText(this, R.string.err_size, Toast.LENGTH_LONG).show();

        } finally {
            dataStream = null;

        }
    }

    /*
     * Call this to delete the original image, will ask the user
     */
    private void handleDelete() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setTitle(R.string.app_name);
        b.setMessage(R.string.confirm_delete);
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                // User clicked OK so go ahead and delete
                ContentResolver cr = getContentResolver();

                if (cr != null)
                    cr.delete(dataStream, null, null);
                else {
                    Toast.makeText(NoteCipher.this, R.string.unable_to_delete_original, Toast.LENGTH_SHORT).show();
                }

            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        b.show();
    }

    void changeTimeoutPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_timeout_prompt_title);
        builder.setMessage(R.string.change_timeout_prompt);
        final NumberPicker input = new NumberPicker(this);
        input.setMinValue(1);
        input.setMaxValue(60);
        input.setValue( mCacheWord.getTimeoutMinutes() );
        builder.setView(input);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int timeout = input.getValue();
                        mCacheWord.setTimeoutMinutes(timeout);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    void showLockScreen() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }

    void lock() {
        closeDatabase();
        notesListView.setAdapter(null);
        System.gc();
        showLockScreen();
    }

    @Override
    public void onCacheWordUninitialized() {
        Log.d(TAG, "onCacheWordUninitialized");
        showLockScreen();
    }

    @Override
    public void onCacheWordLocked() {
        Log.d(TAG, "onCacheWordLocked");
        lock();
    }

    @Override
    public void onCacheWordOpened() {
        Log.d(TAG, "onCacheWordOpened");
        unlockDatabase();

        if (mDbHelper.isOpen()) {
            if (dataStream != null)
                importDataStream();
            else
                fillData();
        }
    }

}
