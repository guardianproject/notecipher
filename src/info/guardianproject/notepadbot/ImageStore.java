package info.guardianproject.notepadbot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class ImageStore extends Activity
{

	private NotesDbAdapter mDbHelper;
	
	private Uri stream;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		openDB(getIntent());
		
		// If originalImageUri is null, we are likely coming from another app via "share"
		
		if (getIntent() != null)
		{
			stream = getIntent().getData();
		
			if(stream == null && getIntent().hasExtra(Intent.EXTRA_STREAM)) {
				stream = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
			}
			
			if (stream != null)
				loadInputStream();
		}
	}
	
	private void loadInputStream()
	{
		try {
			InputStream is = getContentResolver().openInputStream(stream);
			
			byte[] data = null;
			
			
			data = readBytesAndClose (is);
			
			String title = stream.getLastPathSegment();
			String body = stream.getPath();
			
			mDbHelper.createNote(title, body, data);
			
			data = null;
			
			Toast.makeText(this, "Imported new file: " + title, Toast.LENGTH_LONG).show();
			
			mDbHelper.close();

			handleDelete();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	
		} 
		catch (OutOfMemoryError e)
		{
			Toast.makeText(this, "Imported file size is too large", Toast.LENGTH_LONG).show();
		
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
				getContentResolver().delete(stream, null, null);
				finish();
				
            }
        });
		b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

               finish();
            }	
		});
		b.show();
	}
	
	
	private void openDB (Intent intent)
	{
		mDbHelper = new NotesDbAdapter(this);
        
        String password = intent.getStringExtra("pwd");
        
        mDbHelper.open(password);
	}
	
	private static byte[] readBytesAndClose(InputStream in) throws IOException {
	    try {
	        int block = 4 * 1024;
	        ByteArrayOutputStream out = new ByteArrayOutputStream(block);
	        byte[] buff = new byte[block];
	        while (true) {
	            int len = in.read(buff, 0, block);
	            if (len < 0) {
	                break;
	            }
	            out.write(buff, 0, len);
	        }
	        return out.toByteArray();
	    } finally {
	        in.close();
	    }
	}
	
	

	
}
