package squar.com.firebasetest;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import squar.com.firebasetest.token.TokenGenerator;
import squar.com.firebasetest.token.TokenOptions;

/**
 * Created by silong on 11/21/14.
 */
public class TestActivity extends Activity implements Firebase.AuthResultHandler, Firebase.AuthStateListener {

  private TextView mTokenInfo, mMethodOnAuthenticated, mMethodOnAuthenticateError, mMethodOnAuthStateChange, mTimeInfo;

  private Timer mTimer;

  private int currentTime;

  private Handler mHandler = new Handler();

  private Firebase mFirebase;

  private String mCurrentToken;

  private static final int EXPIRATION_TIME = 30;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mTokenInfo = (TextView) findViewById(R.id.token_info);
    mMethodOnAuthenticated = (TextView) findViewById(R.id.method_onAuthenticated_info);
    mMethodOnAuthenticateError = (TextView) findViewById(R.id.method_onAuthenticateError_info);
    mMethodOnAuthStateChange = (TextView) findViewById(R.id.method_onAuthStateChange_info);
    mTimeInfo = (TextView) findViewById(R.id.time_info);
    Firebase.setAndroidContext(this);
    mFirebase = new Firebase(getString(R.string.firebase_url));
    mFirebase.addAuthStateListener(this);
    mTimer = new Timer("Timer");
    mTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        updateTime();
      }
    }, 1000, 1000);
  }

  private void updateTime() {
    currentTime--;
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        if (currentTime > 0) {
          mTimeInfo.setText(String.valueOf(currentTime));
        } else {
          mTimeInfo.setText("token expired");
        }
      }
    });
  }

  private String generateToken(long expireTime) {
    Map<String, Object> authPayload = new HashMap<String, Object>();
    authPayload.put("id", UUID.randomUUID().toString());
    TokenOptions tokenOptions = new TokenOptions();
    tokenOptions.setExpires(new Date(System.currentTimeMillis() + expireTime));
    TokenGenerator tokenGenerator = new TokenGenerator(getString(R.string.firebase_secret_key));
    return tokenGenerator.createToken(authPayload, tokenOptions);
  }

  public void generateToken(View view) {
    mCurrentToken = generateToken(EXPIRATION_TIME * 1000);
    currentTime = EXPIRATION_TIME + 10;
    mTokenInfo.setText("current token: " + mCurrentToken);
  }

  public void authWithCurrentToken(View view) {
    if (TextUtils.isEmpty(mCurrentToken)) {
      Toast.makeText(this, "please generate token first", Toast.LENGTH_SHORT).show();
    } else {
      mFirebase.authWithCustomToken(mCurrentToken, this);
    }
  }

  public void clear(View view) {
    mTokenInfo.setText("");
    mMethodOnAuthenticated.setText("");
    mMethodOnAuthenticateError.setText("");
    mMethodOnAuthStateChange.setText("");
    mTimeInfo.setText("");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTimer.cancel();
    mFirebase.removeAuthStateListener(this);
  }

  @Override
  public void onAuthStateChanged(AuthData authData) {
    mMethodOnAuthStateChange.setText("onAuthStateChanged called at " + formatDate(System.currentTimeMillis())
        + (authData == null ? " token expired" : (", expire at " + formatDate(authData.getExpires() * 1000))));
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    mMethodOnAuthenticated.setText("onAuthenticated called at " + formatDate(System.currentTimeMillis())
        + ", token expire at " + formatDate(authData.getExpires() * 1000));
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    mMethodOnAuthenticateError
        .setText("onAuthenticationError called at:" + formatDate(System.currentTimeMillis()) + ", error:" + firebaseError.getMessage());
  }

  private String formatDate(long dateInMilis) {
    return new SimpleDateFormat("HH:mm:ss").format(dateInMilis);
  }
}
