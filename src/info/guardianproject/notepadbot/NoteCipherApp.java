
package info.guardianproject.notepadbot;

import android.app.Application;

public class NoteCipherApp extends Application {
    @Override
    public void onCreate() {
        // Apply the Google PRNG fixes to properly seed SecureRandom
        PRNGFixes.apply();
    }
}
