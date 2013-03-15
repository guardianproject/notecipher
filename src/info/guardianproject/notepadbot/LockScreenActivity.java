
package info.guardianproject.notepadbot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

public class LockScreenActivity extends Activity implements ICacheWordSubscriber {
    private static final String TAG = "LockScreenActivity";

    private EditText mEnterPassphrase;
    private EditText mNewPassphrase;
    private EditText mConfirmNewPassphrase;
    private View mViewCreatePassphrase;
    private View mViewEnterPassphrase;
    private Button mBtnOpen;
    private CacheWordHandler mCacheWord;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        mCacheWord = new CacheWordHandler(this);

        mViewCreatePassphrase = findViewById(R.id.llCreatePassphrase);
        mViewEnterPassphrase = findViewById(R.id.llEnterPassphrase);

        mEnterPassphrase = (EditText) findViewById(R.id.editEnterPassphrase);
        mNewPassphrase = (EditText) findViewById(R.id.editNewPassphrase);
        mConfirmNewPassphrase = (EditText) findViewById(R.id.editConfirmNewPassphrase);

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

    @Override
    public void onCacheWordUninitializedEvent() {
        initializePassphrase();

    }

    @Override
    public void onCacheWordLockedEvent() {
        promptPassphrase();
    }

    @Override
    public void onCacheWordUnLockedEvent() {
        Intent intent = (Intent) getIntent().getParcelableExtra("originalIntent");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        LockScreenActivity.this.overridePendingTransition(0, 0);

    }

    private void initializePassphrase() {
        // Passphrase is not set, so allow the user to create one!

        View viewCreatePassphrase = findViewById(R.id.llCreatePassphrase);
        viewCreatePassphrase.setVisibility(View.VISIBLE);
        mViewEnterPassphrase.setVisibility(View.GONE);

        Button btnCreate = (Button) findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                // Compare the two text fields!
                if (!mNewPassphrase.getText().toString()
                        .equals(mConfirmNewPassphrase.getText().toString()))
                {
                    Toast.makeText(LockScreenActivity.this,
                            getString(R.string.lock_screen_passphrases_not_matching),
                            Toast.LENGTH_SHORT).show();
                    mNewPassphrase.setText("");
                    mConfirmNewPassphrase.setText("");
                    mNewPassphrase.requestFocus();
                    return; // Try again...
                }

                try {
                    mCacheWord.setPassphrase(mNewPassphrase.getText().toString().toCharArray());
                } catch (GeneralSecurityException e) {
                    // TODO initialization failed
                    Log.e(TAG, "Cacheword pass initialization failed: " + e.getMessage());
                }
            }
        });
    }

    private void promptPassphrase() {
        mViewCreatePassphrase.setVisibility(View.GONE);
        mViewEnterPassphrase.setVisibility(View.VISIBLE);

        mBtnOpen = (Button) findViewById(R.id.btnOpen);
        mBtnOpen.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                // Check passphrase
                try {
                    mCacheWord.setPassphrase(mEnterPassphrase.getText().toString().toCharArray());
                } catch (GeneralSecurityException e) {
                    mEnterPassphrase.setText("");
                    // TODO implement try again and wipe if fail
                    Log.e(TAG, "Cacheword pass verification failed: " + e.getMessage());
                    return;
                }
            }
        });

        mEnterPassphrase.setOnEditorActionListener(new OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_GO)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                    Handler threadHandler = new Handler();

                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0, new ResultReceiver(
                            threadHandler)
                    {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData)
                        {
                            super.onReceiveResult(resultCode, resultData);
                            mBtnOpen.performClick();
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }
}
