/*
 * Copyright (C) 2008 Google Inc.
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

package info.guardianproject.notepadbot;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NoteEdit extends Activity {

	private EditText mTitleText;
    private EditText mBodyText;
    private ImageView mImageView;
    private byte[] blob;
    
    private long mRowId = -1;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (savedInstanceState != null)
        	mRowId = savedInstanceState.getLong(NotesDbAdapter.KEY_ROWID);
       
			
   	 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
    //    menu.add(0, INSERT_ID, 0, R.string.menu_insert);
     //   menu.add(0, REKEY_ID, 0, R.string.menu_rekey);
        
        
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        /*
        case INSERT_ID:
            createNote();
            return true;
        case REKEY_ID:
            showRekeyDialog();
            return true;           
            */ 
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    
    private void setupView (boolean hasImage)
    {
    	if (hasImage)
      	  setContentView(R.layout.note_edit_image);
    	else
    	  setContentView(R.layout.note_edit);
          
          
          mTitleText = (EditText) findViewById(R.id.title);
          mBodyText = (EditText) findViewById(R.id.body);
          mImageView = (ImageView) findViewById(R.id.odata);
    }
    
    private void populateFields() {
    	try
    	{
    		
	            Cursor note = NotesDbAdapter.getInstance(this).fetchNote(mRowId);
	            startManagingCursor(note);
	
	            blob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
	
	            setupView(blob != null);
	            
	            if (blob != null)
	            {
	            	
	            	
	            	// Load up the image's dimensions not the image itself
					BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
					bmpFactoryOptions.inSampleSize = 3;
				
	            	Bitmap blobb = BitmapFactory.decodeByteArray(blob, 0, blob.length, bmpFactoryOptions);
	
	            	mImageView.setImageBitmap(blobb);
	            }
	            else
	            {
	            	 mBodyText.setText(note.getString(
	 	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
	 	           
	            }
	            
	            mTitleText.setText(note.getString(
	    	            note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	            
	        
    	}
    	catch (Exception e)
    	{
    		Log.e("notepadbot", "error populating",e);
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
       if (mRowId != -1)
    		   outState.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
       
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        Bundle extras = getIntent().getExtras();   
	
        if (mRowId != -1)
        {
        	populateFields();
        }
        else if (extras != null)
		{
			mRowId =  extras.getLong(NotesDbAdapter.KEY_ROWID);
			populateFields();
		}
		else
		{
	   	 	setupView(false);
		}
   

		
        populateFields();
    }
    
    private void saveState() {
    	
    	if (mTitleText != null && mTitleText.getText() != null)
    	{
	        String title = mTitleText.getText().toString();
	        String body = "";
	        
	        if (mBodyText != null)
	        	body = mBodyText.getText().toString();
	
	        if (title != null && title.length() > 0)
	        {
		        if (mRowId == -1) {
		            long id = NotesDbAdapter.getInstance(this).createNote(title, body, null, null);
		            if (id > 0) {
		                mRowId = id;
		            }
		        } else {
		        	NotesDbAdapter.getInstance(this).updateNote(mRowId, title, body, null, null);
		        }
	        }
	        
    	}
    }
    
    private void shareEntry()
    {
         if (blob != null)
         {
        	 try
        	 {
        		 NoteUtils.shareImage(this, blob);
        	 }
        	 catch (Exception e)
        	 {
        		 Toast.makeText(this, "Error exporting image: " + e.getMessage(), Toast.LENGTH_LONG).show();

        	 }
         }
         else
         {
        	 String body = mBodyText.getText().toString();
        	 NoteUtils.shareText(this, body);
         }
         
        
    }
    
    private void viewEntry(long id)
    {
    	 
         if (blob != null)
         {
        	 String title = mTitleText.getText().toString();
        	 NoteUtils.savePublicImage(this, title, blob);
        	 
         }
         
        
    }
    
    
       
    
}
