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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NoteEdit extends Activity {

	private EditText mTitleText;
    private EditText mBodyText;
    private ImageView mImageView;
    private byte[] mBlob;
    private String mMimeType;
    
    private long mRowId = -1;
    
    private static final int SAVE_ID = Menu.FIRST;
    private static final int SHARE_ID = Menu.FIRST + 1;
    private static final int VIEW_ID = Menu.FIRST + 2;
    private static final int BIGGER_ID = Menu.FIRST + 3;
    private static final int SMALLER_ID = Menu.FIRST + 4;
    
    
    private final static String ZERO_TEXT = "*******************";
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (savedInstanceState != null)
        	mRowId = savedInstanceState.getLong(NotesDbAdapter.KEY_ROWID);
       
			
   	 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SAVE_ID, 0, R.string.menu_save);
        
        if (mBlob != null)
        	menu.add(0, VIEW_ID, 0, R.string.menu_view);
        
		menu.add(0, SHARE_ID, 0, R.string.menu_share);
		menu.add(0, SMALLER_ID, 0, R.string.menu_smaller);
		menu.add(0, BIGGER_ID, 0, R.string.menu_bigger);
        
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case SAVE_ID:
    		saveState();
	        return true;
    	case SHARE_ID:
    		shareEntry();
	        return true;
    	case VIEW_ID:
    		viewEntry();
	        return true;
    	case BIGGER_ID:
    		changeTextSize(1.1f);
	        return true;
    	case SMALLER_ID:
    		changeTextSize(.9f);
	        return true;
		}
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    private void changeTextSize (float factor)
    {
    	float pxSize = mBodyText.getTextSize();
    	pxSize *= factor;
    	
    	mBodyText.setTextSize(pxSize);
    }
    
    private void setupView (boolean hasImage)
    {

        
    	if (hasImage)
    	{
      	  setContentView(R.layout.note_edit_image);

          mImageView = (ImageView) findViewById(R.id.odata);
    	}
    	else
    	{
    	  setContentView(R.layout.note_edit);

          mBodyText = (EditText) findViewById(R.id.body);
    	} 
    	

        mTitleText = (EditText) findViewById(R.id.title);
          
    }
    
    private void populateFields() {
    	try
    	{
    		
	            Cursor note = NotesDbAdapter.getInstance(this).fetchNote(mRowId);
	            startManagingCursor(note);
	
	            mBlob = note.getBlob(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATA));
	
	            mMimeType = note.getString(
 	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE));
	            
	            if (mMimeType == null)
	            	mMimeType = "text/plain";
	            
	            boolean isImage = mMimeType.startsWith("image");
	            

            	setupView(isImage);
            	
	            if (isImage)
	            {
		            	
	            	// Load up the image's dimensions not the image itself
					BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
					
					if (mBlob.length > 100000)
						bmpFactoryOptions.inSampleSize = 4;
					else
						bmpFactoryOptions.inSampleSize = 2;
					
	            	Bitmap blobb = BitmapFactory.decodeByteArray(mBlob, 0, mBlob.length, bmpFactoryOptions);
	
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
    		Toast.makeText(this, "Something went wrong when loading your note: " + e.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        saveState();
        
        if (mRowId != -1)
    		   outState.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
       
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
	    saveState();
	        
        if (mTitleText != null)
        	mTitleText.setText(ZERO_TEXT);
        
        if (mBodyText != null)
        	mBodyText.setText(ZERO_TEXT);
        
        if (mImageView != null)
        	mImageView.setImageBitmap(null);
        
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
   

    }
    
    private void saveState() {
    	
    	if ((mTitleText != null && mTitleText.getText() != null && mTitleText.getText().length() > 0)
    			|| (mBodyText != null && mBodyText.getText() != null && mBodyText.getText().length() > 0)
    			)
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
         if (mBlob != null)
         {
        	 try
        	 {
        		 NoteUtils.shareData(this, mTitleText.getText().toString(), mMimeType, mBlob);
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
    
    private void viewEntry()
    {
    	 
         if (mBlob != null)
         {
        	 String title = mTitleText.getText().toString();
        	 NoteUtils.savePublicFile(this, title, mMimeType, mBlob);
        	 
         }
         
        
    }
    
    
       
    
}
