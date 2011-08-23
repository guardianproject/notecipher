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

import info.guardianproject.database.sqlcipher.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class Notepadbot extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int REKEY_ID = Menu.FIRST + 2;
    private static final int SHARE_ID = Menu.FIRST + 3;
    private static final int VIEW_ID = Menu.FIRST + 4;
    private static final int LOCK_ID = Menu.FIRST + 5;
    
    public static final String TAG = "notecipher";
    
    private NotesDbAdapter mDbHelper;
    
    private Uri dataStream;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent() != null)
		{
			
			if(getIntent().hasExtra(Intent.EXTRA_STREAM)) {
				dataStream = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
			}
			else
				dataStream = getIntent().getData();
			
		}
        
        SQLiteDatabase.loadLibs(this);
        
        setContentView(R.layout.notes_list);

        registerForContextMenu(getListView());

		if (savedInstanceState != null)
		{
			
		}
    }
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		

    	mDbHelper = NotesDbAdapter.getInstance(this);
    	
    	if (!mDbHelper.isOpen())
			showPassword();
    	else if (dataStream != null)
    		importDataStream();
    	else
    		fillData();
    	
	
	}


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		 findViewById(R.id.listlayout).setOnTouchListener(new OnTouchListener ()
	        {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					
					if (mDbHelper != null && mDbHelper.isOpen())
						createNote();
					
					return false;
				}
	        	
	        }
	        		
	        );
	}



	private void showPassword ()
    {
		String dialogMessage;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean firstTime = prefs.getBoolean("first_time",true);
		
		if (firstTime)
		{
			dialogMessage = getString(R.string.new_pin);
			Editor pEdit = prefs.edit();
			pEdit.putBoolean("first_time",false);
			pEdit.commit();
		}
		else
			dialogMessage = getString(R.string.enter_pin);

		
    	 // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setView(textEntryView)
            .setMessage(dialogMessage)
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));
                	String password = eText.getText().toString();
                	
                	unlockDatabase(password);
                	
                	
                	eText.setText("");
                	System.gc();
                	
                }
            })
            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create().show();
    }
	
	private void showRekeyDialog ()
    {
    	 // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setView(textEntryView)
            .setMessage(getString(R.string.rekey_message))
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));

                	String newPassword = eText.getText().toString();
                	
                	rekeyDatabase(newPassword);
                	
                	eText.setText("");
                	System.gc();
                	
                }
            })
            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create().show();
    }
    
	private void lockDatabase ()
	{
		mDbHelper.close();
		mDbHelper = null;
		
		finish();
		
	}
	
    private void unlockDatabase (String password)
    {

    	try
    	{
    	
    		mDbHelper.open(password);
    		
    		if (dataStream != null)
        		importDataStream();
    		else
    			fillData();
    	}
    	catch (Exception e)
    	{
    		Toast.makeText(this, getString(R.string.err_pin), Toast.LENGTH_LONG).show();
    		showPassword();
    	}
    }
    
    private void rekeyDatabase (String password)
    {

    	try
    	{
    		Toast.makeText(this, getString(R.string.do_rekey), Toast.LENGTH_LONG).show();

    	    	mDbHelper.rekey(password);    		

    	}
    	catch (Exception e)
    	{
    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

    	}
    }
    
    private void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
        
        
        if (notes.isEmpty())
        {
        	Toast.makeText(this, getString(R.string.on_start), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, REKEY_ID, 0, R.string.menu_rekey);
        menu.add(0, LOCK_ID, 0, R.string.menu_lock);
        
        
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
            createNote();
            return true;
        case REKEY_ID:
            showRekeyDialog();
            return true;  
        case LOCK_ID:
            lockDatabase();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEW_ID, 0, R.string.menu_view);
		menu.add(0, SHARE_ID, 0, R.string.menu_share);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info;
    	
		switch(item.getItemId()) {
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
	
    private void shareEntry(long id)
    {
    	Cursor note = mDbHelper.fetchNote(id);
    	 startManagingCursor(note);
    	 
    	 byte[] blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
         
         if (blob != null)
         {
        	 try
        	 {
        		 NoteUtils.shareImage(this, blob);
        	 }
        	 catch (IOException e)
        	 {
        		 Toast.makeText(this, getString(R.string.err_export) + e.getMessage(), Toast.LENGTH_LONG).show();
        	 }
         }
         else
         {
        	 String body = note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
        	 NoteUtils.shareText(this, body);
         }
         
         note.close();
    }
    
    private void viewEntry(long id)
    {
    	Cursor note = mDbHelper.fetchNote(id);
    	 startManagingCursor(note);
    	 
    	 byte[] blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
         
         if (blob != null)
         {
        	 String title = note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
        	 
        	 NoteUtils.savePublicImage(this, title, blob);
        	 
         }
         
         note.close();
    }
    
    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /*
     * Called after the return from creating a new note
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
    	mDbHelper = NotesDbAdapter.getInstance(this);

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
		
		NoteUtils.cleanupTmp(this);
		
	}

	private void importDataStream()
	{
		try {
			InputStream is = getContentResolver().openInputStream(dataStream);
			
			String mimeType = getContentResolver().getType(dataStream);
			
			byte[] data = NoteUtils.readBytesAndClose (is);
			
			String title = dataStream.getLastPathSegment();
			String body = dataStream.getPath();
			
			NotesDbAdapter.getInstance(this).createNote(title, body, data, mimeType);
			
			Toast.makeText(this, getString(R.string.on_import) + ": " + title, Toast.LENGTH_LONG).show();

			handleDelete();

			data = null;
			dataStream = null;
			title = null;
			body = null;
			
			System.gc();
			
			fillData();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);

	
		} 
		catch (OutOfMemoryError e)
		{
			Toast.makeText(this, getString(R.string.err_size), Toast.LENGTH_LONG).show();
		
		}
		finally
		{
			//finish();
			
		}
	}
	
	/*
	 * Call this to delete the original image, will ask the user
	 */
	private void handleDelete() 
	{
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setIcon(android.R.drawable.ic_dialog_alert);
		b.setTitle(getString(R.string.app_name));
		b.setMessage(getString(R.string.confirm_delete));
		b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // User clicked OK so go ahead and delete
				getContentResolver().delete(dataStream, null, null);
				
				
            }
        });
		b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

              
            }	
		});
		b.show();
	}
	
	
	
	

   
}
