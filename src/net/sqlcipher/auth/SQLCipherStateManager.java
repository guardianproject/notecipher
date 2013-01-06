package net.sqlcipher.auth;

import info.guardianproject.notecipher.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SQLCipherStateManager
{
	protected static String TAG = "SQLCipherStateManager";

    //strong passphrase config variables
	private final static int MIN_PASS_LENGTH = 6;
	private final static int MAX_PASS_ATTEMPTS = 3;
	private final static int PASS_RETRY_WAIT_TIMEOUT = 30000;
    private int currentPassAttempts = 0;
    private boolean unlockedOK = false;
    
    
    private SQLCipherOwner _sqOwner;
    
    private Context _context;
	
    
    public SQLCipherStateManager (Context context, SQLCipherOwner sqOwner)
    {
    	_context = context;
    	_sqOwner = sqOwner;
    }
    

    public void showPassword ()
    {
		String dialogMessage;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		
		boolean firstTime = prefs.getBoolean("first_time",true);
		
		Log.d(TAG, "showing Password dialog - this is attempt " + currentPassAttempts + " and first time ? "+ firstTime);
		
		if (currentPassAttempts >= MAX_PASS_ATTEMPTS)
		{		
			Log.d(TAG, "Too many attempts ... ");
			
			try { Thread.sleep(PASS_RETRY_WAIT_TIMEOUT); }
			catch (Exception e){};
		}
		
		
		if (firstTime)
		{
			dialogMessage = _context.getString(R.string.new_pass);
			
			
			 // This example shows how to add a custom layout to an AlertDialog
	        LayoutInflater factory = LayoutInflater.from(_context);
	        final View textEntryView = factory.inflate(R.layout.sqlcipher_password_dialog, null);
	        final AlertDialog new_p_dialog = new AlertDialog.Builder(_context)
	            .setTitle(_context.getString(R.string.app_name))
	            .setView(textEntryView)
	            .setMessage(dialogMessage)
	            .setNegativeButton(_context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	
	                    /* User clicked cancel so do some stuff */
	                	_sqOwner.cancel();
	                }
	            })
	            .create();
	        
	        //We need to do this, so we can reference to our local, final variable new_p_dialog, in the onclicklistener code
		       //
	       new_p_dialog.setButton(DialogInterface.BUTTON_POSITIVE,_context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));
                	String passphrase = eText.getText().toString();
                	
                	if (goodPassphrase (passphrase))
                	{
                		try {
							_sqOwner.unlockDatabase(passphrase);
							//If we reach here, we didnt throw an exception
							unlockedOK = true;
						} catch (Exception e) {
							showPasswordDialogMessageToast(e.getMessage());
							unlockedOK = false;
						}                	
                		eText.setText("");
                		System.gc();
                		
                		if (unlockedOK) {
                			//we're good so we can flag this is not first_time anymore
                			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
                			Editor pEdit = prefs.edit();
            				pEdit.putBoolean("first_time",false);
            				pEdit.commit();
            				
            				showPasswordDialogMessageToast(_context.getString(R.string.passphrase_accepted_database_created_));
            				
                		} else {
                			
                			//Dismiss ourselves, then re-spawn.
                			new_p_dialog.dismiss();
                    		//show password dialog again
                    		showPassword();
                		}
            			
                	}
                	else
                	{	 
                		//Dismiss ourselves, then re-spawn.
                		new_p_dialog.dismiss();
                		//show password dialog again
                		showPassword();
                	}
                	
                }
            });
	        
	        
	        //Show the dialog.
	        new_p_dialog.show();
			
		}
		else
		{
			dialogMessage = _context.getString(R.string.enter_pass);
		
	    	 // This example shows how to add a custom layout to an AlertDialog
	        LayoutInflater factory = LayoutInflater.from(_context);
	        final View textEntryView = factory.inflate(R.layout.sqlcipher_password_dialog, null);
	        
	        final AlertDialog p_dialog = new AlertDialog.Builder(_context)
	            .setTitle(_context.getString(R.string.app_name))
	            .setView(textEntryView)
	            .setMessage(dialogMessage)
	            .setNegativeButton(_context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	
	                    /* User clicked cancel so do some stuff */
	                	_sqOwner.cancel();
	                }
	            })
	            .create();
	        
	       //We need to do this, so we can reference to our local, final variable p_dialog, in the onclicklistener code
	       //
	       p_dialog.setButton(DialogInterface.BUTTON_POSITIVE, _context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	Log.d(TAG, "Checking password.");
                	
                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));
                	String passphrase = eText.getText().toString();
                	
                	try {
						_sqOwner.unlockDatabase(passphrase);
						
						//If we reach here, we didnt throw an exception 
						currentPassAttempts = 0;
						unlockedOK = true;
						
					} catch (Exception e) {
						
						Log.d(TAG, "WRONG PASSWORD!\nWe caught exception:" + e.getMessage());
						e.printStackTrace();
						
						currentPassAttempts++;
						showPasswordDialogMessageToast(_context.getString(R.string.wrong_password_));

						unlockedOK = false;
					}   
                	
                	eText.setText("");
                	System.gc();                	
                	
                	//Respawn if we didn't unlock the database.
                	if (! unlockedOK) {
                		
                		//Dismiss ourselves, then re-spawn.
                		p_dialog.dismiss();
                		
                		showPassword();
                	} else {
                		
                		showPasswordDialogMessageToast(_context.getString(R.string.accepted_password_database_unlocked_));
                	}
                	
                }
            });
	        
	        //Show the dialog.
	        p_dialog.show();
		}
    }

	private boolean goodPassphrase (String pass)
	{
		
		 boolean upper = false;
		    boolean lower = false;
		    boolean number = false;
		    for (char c : pass.toCharArray()) {
		      if (Character.isUpperCase(c)) {
		        upper = true;
		      } else if (Character.isLowerCase(c)) {
		        lower = true;
		      } else if (Character.isDigit(c)) {
		        number = true;
		      }
		    }
		

		if (pass.length() < MIN_PASS_LENGTH)
		{
			//should we support some user string message here?
			showPasswordDialogMessageToast(_context.getString(R.string.pass_err_length));
			return false;
		}
		else if (!upper)
		{
			showPasswordDialogMessageToast(_context.getString(R.string.pass_err_upper));
			return false;
		}
		else if (!lower)
		{
			showPasswordDialogMessageToast(_context.getString(R.string.pass_err_lower));
			return false;
		}
		else if (!number)
		{
			showPasswordDialogMessageToast(_context.getString(R.string.pass_err_num));
			return false;
		}
		
		
		 //if it got here, then must be okay
		return true;
	}
	
	public void showRekeyDialog ()
    {
    	 // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(_context);
        final View textEntryView = factory.inflate(R.layout.sqlcipher_password_dialog, null);
        new AlertDialog.Builder(_context)
            .setTitle(_context.getString(R.string.app_name))
            .setView(textEntryView)
            .setMessage(_context.getString(R.string.rekey_message))
            .setPositiveButton(_context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));

                	String newPassword = eText.getText().toString();
                	
                	if (goodPassphrase(newPassword))
                	{
                		try {
							_sqOwner.rekeyDatabase(newPassword);
							//If we reach here, we didnt throw an exception 
							unlockedOK = true;
						} catch (Exception e) {
							showPasswordDialogMessageToast(e.getMessage());
							unlockedOK = false;
						}
                	
                		eText.setText("");
                		System.gc();
                	}
                	else {
                	
                		showRekeyDialog();
                		
                	}
                	
                }
            })
            .setNegativeButton(_context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create().show();
    }
    
	
	private void showPasswordDialogMessageToast (String msg)
	{
		Toast.makeText(_context, msg, Toast.LENGTH_LONG).show();
	}
}
