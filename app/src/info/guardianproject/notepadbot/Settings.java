package info.guardianproject.notepadbot;


import info.guardianproject.cacheword.Constants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class Settings extends SherlockPreferenceActivity {
	
	public static final String LANG_SEL_KEY = "langSelected";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				
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
						}
					})
					.commit();
		} else {
			// Otherwise load the preferences.xml in the Activity like in previous android versions
			addPreferencesFromResource(R.xml.settings);
			findPreference(Constants.SHARED_PREFS_TIMEOUT).setOnPreferenceClickListener(changeLockTimeoutListener);
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
						
						return true;
					}
		};
}