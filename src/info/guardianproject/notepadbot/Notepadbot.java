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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.guardianproject.database.sqlcipher.SQLiteDatabase;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    
    

    private NotesDbAdapter mDbHelper;
    
    private String password;
    private Uri dataStream;
    private Uri tmpImageUri;
    
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

		
    }
    
    
    private void loadData ()
    {
    	
    	Intent passingIntent = new Intent(this,ImageStore.class);

		passingIntent.putExtra("pwd", password);
		passingIntent.setData(dataStream);
		startActivityForResult(passingIntent, 1);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
		if (mDbHelper == null)
			showPassword();
		
	
	}


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		 findViewById(R.id.listlayout).setOnTouchListener(new OnTouchListener ()
	        {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					createNote();
					return false;
				}
	        	
	        }
	        		
	        );
	}



	private void showPassword ()
    {
    	 // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setView(textEntryView)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	password = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit)).getText().toString();
                	
                	unlockDatabase(password);
                	
                	if (dataStream != null)
        				loadData();
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	password = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit)).getText().toString();
                	
                	rekeyDatabase(password);
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create().show();
    }
    
    private void unlockDatabase (String password)
    {
    	if (mDbHelper == null)
    		mDbHelper = new NotesDbAdapter(this);

    	try
    	{
    	
    		mDbHelper.open(password);
    		fillData();
    	}
    	catch (Exception e)
    	{
    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    		showPassword();
    	}
    }
    
    private void rekeyDatabase (String password)
    {

    	try
    	{
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
        	createNote();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, REKEY_ID, 0, R.string.menu_rekey);
        
        
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
        	 shareImage(blob);
        	 
         }
         else
         {
        	 String body = note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
        	 shareText(body);
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
        	 
        	 savePublicImage(title, blob);
        	 
         }
         
         note.close();
    }
    
    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        
        i.putExtra("pwd", password);
        
       // mDbHelper.close();
        
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra("pwd", password);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
      //  mDbHelper.open(password);
        fillData();
    }
    
    
    /*
	 * When the user selects the Share menu item
	 * Uses saveTmpImage (overwriting what is already there) and uses the standard Android Share Intent
	 */
    private void shareImage(byte[] outdata) {
    	
    	if (saveTmpImage(outdata)) {
        	Intent share = new Intent(Intent.ACTION_SEND);
        	share.setType("image/jpeg");
        	share.putExtra(Intent.EXTRA_STREAM, tmpImageUri);
        	startActivity(Intent.createChooser(share, "Share Image"));    	
    	} else {
    		Toast t = Toast.makeText(this,"Saving Temporary File Failed!", Toast.LENGTH_SHORT); 
    		t.show();
    	}
    }
    
    private void shareText(String outdata) {
    	
    	Intent share = new Intent(Intent.ACTION_SEND);
    	share.setType("text/plain");
    	share.putExtra(Intent.EXTRA_TEXT, outdata);
    	startActivity(Intent.createChooser(share, "Share Text"));    	
	
    }
    
    

	@Override
	protected void onStop() {
		super.onStop();
		
		
		//delete temp share file
		  File file = new File(getExternalFilesDirEclair(null), "nctemp.jpg");
		  if (file.exists())
			  file.delete();
		
	}


	public File getExternalFilesDirEclair(Object object) {
		
		
		String packageName = getPackageName();
		File externalPath = Environment.getExternalStorageDirectory();
		File appFiles = new File(externalPath.getAbsolutePath() +
	                         "/Android/data/" + packageName + "/files");
		
		if (!appFiles.exists())
			appFiles.mkdirs();
		
		return appFiles;
	}

	private boolean saveTmpImage(byte[] outdata) {
		
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = getExternalFilesDirEclair(null);
        File file = new File(path, "nctemp.jpg");

        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = new ByteArrayInputStream(outdata);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();

	    	tmpImageUri = Uri.fromFile(file);
	    	
	    	return true;

        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
            
            return false;
        }
    }
	
	
	private boolean savePublicImage(String title, byte[] outdata) {
		
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
	    File path = getExternalFilesDirEclair("NoteCipher");
        File file = new File(path, title);

        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = new ByteArrayInputStream(outdata);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
            
            /*

	    	  // Tell the media scanner about the new file so that it is
	        // immediately available to the user.
	        MediaScannerConnection.scanFile(this,
	                new String[] { file.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	                Log.i("ExternalStorage", "Scanned " + path + ":");
	                Log.i("ExternalStorage", "-> uri=" + uri);
	                
	                Intent intent = new Intent();
	    	        intent.setAction(android.content.Intent.ACTION_VIEW);
	    	        intent.setDataAndType(uri, "image/jpeg");
	    	        startActivity(intent);
	            }
	        });
	        
	        // Tell the media scanner about the new file so that it is
	        // immediately available to the user.
	        MediaScannerConnection.scanFile(this,
	                new String[] { path.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	               
	            }
	        });
	        */
	    	
	    	return true;

        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
            
            return false;
        }
    }

   
}
