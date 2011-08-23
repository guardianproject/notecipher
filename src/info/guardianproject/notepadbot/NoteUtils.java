package info.guardianproject.notepadbot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class NoteUtils {
	 /*
		 * When the user selects the Share menu item
		 * Uses saveTmpImage (overwriting what is already there) and uses the standard Android Share Intent
		 */
	    public static void shareImage(Context ctx, byte[] outdata) throws IOException
	    {
	    	
	    	Uri tmpImageUri = saveTmpImage(ctx, outdata);
	    	
	    	if (tmpImageUri != null) {
	        	Intent share = new Intent(Intent.ACTION_SEND);
	        	share.setType("image/jpeg");
	        	share.putExtra(Intent.EXTRA_STREAM, tmpImageUri);
	        	ctx.startActivity(Intent.createChooser(share, "Share Image"));    	
	    	} else {
	    		Toast t = Toast.makeText(ctx,"Saving Temporary File Failed!", Toast.LENGTH_SHORT); 
	    		t.show();
	    	}
	    }
	    
	    public static void shareText(Context ctx, String outdata) {
	    	
	    	Intent share = new Intent(Intent.ACTION_SEND);
	    	share.setType("text/plain");
	    	share.putExtra(Intent.EXTRA_TEXT, outdata);
	    	ctx.startActivity(Intent.createChooser(share, "Share Text"));    	
		
	    }
	    
	    

	public static File getExternalFilesDirEclair(Context ctx, Object object) {
		
		
		String packageName = ctx.getPackageName();
		File externalPath = Environment.getExternalStorageDirectory();
		File appFiles = new File(externalPath.getAbsolutePath() +
	                         "/Android/data/" + packageName + "/files");
		
		if (!appFiles.exists())
			appFiles.mkdirs();
		
		return appFiles;
	}

	public static boolean cleanupTmp (Context ctx)
	{

		//delete temp share file
		  File file = new File(NoteUtils.getExternalFilesDirEclair(ctx, null), "nctemp.jpg");
		  if (file.exists())
		  {
			  file.delete();
			  return true;
		  }
		  else
			  return false;
	}
	
	public static Uri saveTmpImage(Context ctx, byte[] outdata) throws IOException
	{
		
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = getExternalFilesDirEclair(ctx, null);
        File file = new File(path, "nctemp.jpg");

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

    	Uri tmpImageUri = Uri.fromFile(file);
    	
    	return tmpImageUri;

      
    }
	
	
	public static boolean savePublicImage(Context ctx, String title, byte[] outdata) {
		
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
	    File path = getExternalFilesDirEclair(ctx, "NoteCipher");
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
	
	public static byte[] readBytesAndClose(InputStream in) throws IOException {
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
