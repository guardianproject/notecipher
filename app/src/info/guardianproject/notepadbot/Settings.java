package info.guardianproject.notepadbot;


import net.simonvt.numberpicker.NumberPicker;
import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.Constants;
import info.guardianproject.cacheword.ICacheWordSubscriber;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class Settings extends SherlockPreferenceActivity implements ICacheWordSubscriber {
	
	public static final String LANG_SEL_KEY = "langSelected";
	
	private CacheWordActivityHandler mCacheWord;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mCacheWord = new CacheWordActivityHandler(this, ((App)getApplication()).getCWSettings());
		
		// If in android 3+ use a preference fragment which is the new recommended way
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new PreferenceFragment() {
						@Override
						public void onCreate(final Bundle savedInstanceState) {
							super.onCreate(savedInstanceState);
							addPreferencesFromResource(R.xml.settings);
							findPreference(Constants.SHARED_PREFS_TIMEOUT)
								.setOnPreferenceClickListener(changeLockTimeoutListener);
							findPreference(Constants.SHARED_PREFS_VIBRATE)
								.setOnPreferenceChangeListener(vibrateChangeListener);
						}
					})
					.commit();
		} else {
			// Otherwise load the preferences.xml in the Activity like in previous android versions
			addPreferencesFromResource(R.xml.settings);
			findPreference(Constants.SHARED_PREFS_TIMEOUT)
				.setOnPreferenceClickListener(changeLockTimeoutListener);
			findPreference(Constants.SHARED_PREFS_VIBRATE)
				.setOnPreferenceChangeListener(vibrateChangeListener);
		}
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		// reload preferences on exit from settings screen
		Context context = getApplicationContext();
		loadSettings(context);
		super.onDestroy();
	}

	/** Loads user settings to app. Called when settings change and users exits from 
	 *  settings screen or when the app first starts. 
	 *  */
	public static void loadSettings(Context context) {
		
	}
	
	private Preference.OnPreferenceClickListener changeLockTimeoutListener = 
			new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference pref) {
						changeTimeoutPrompt();
						return true;
					}
	};
	
	private Preference.OnPreferenceChangeListener vibrateChangeListener = 
			new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference pref, Object newValue) {
			// save option internally in cacheword as well
			mCacheWord.setVibrateSetting((Boolean) newValue);
			return true;
		}
	};
	
	public static final boolean getNoteLinesOption(Context context) {
		boolean defValue = context.getResources().getBoolean(R.bool.notecipher_uselines_default);
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(NConstants.SHARED_PREFS_NOTELINES, defValue);
	}
	
	private void changeTimeoutPrompt() {
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

	@Override
	public void onCacheWordUninitialized() {
		Log.d(NConstants.TAG, "onCacheWordUninitialized");
		System.gc();
		showLockScreen();
	}

	@Override
	public void onCacheWordLocked() {
		Log.d(NConstants.TAG, "onCacheWordLocked");
		System.gc();
		showLockScreen();
	}

	@Override
	public void onCacheWordOpened() { 
		Log.d(NConstants.TAG, "onCacheWordOpened");
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
    
    void showLockScreen() {
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }
}