package squar.com.firebasetest;

import com.google.gson.Gson;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import squar.com.firebasetest.token.TokenGenerator;
import squar.com.firebasetest.token.TokenOptions;

/**
 * Created by silong on 11/21/14.
 */
public class TestActivity extends Activity implements Firebase.AuthResultHandler, Firebase.AuthStateListener {

  private Firebase mFirebase;

  private static final int EXPIRATION_TIME = 3600000;

  private TextView mTextInfo;

  private Gson GSON = new Gson();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mTextInfo = (TextView) findViewById(R.id.text_info);
    Firebase.setAndroidContext(this);
    mFirebase = new Firebase(getString(R.string.firebase_url));
    //    mFirebase.addAuthStateListener(this);
    //    syncFromFirebase();
  }

  public void clear(View v) {
    mTextInfo.setText("");
  }

  private void authenticate() {
    Map<String, Object> authPayload = new HashMap<String, Object>();
    authPayload.put("uid", UUID.randomUUID().toString());
    TokenOptions tokenOptions = new TokenOptions();
    tokenOptions.setExpires(new Date(System.currentTimeMillis() + EXPIRATION_TIME));
    TokenGenerator tokenGenerator = new TokenGenerator(getString(R.string.firebase_secret_key));
    mFirebase.authWithCustomToken(tokenGenerator.createToken(authPayload, tokenOptions), this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mFirebase.removeAuthStateListener(this);
  }

  @Override
  public void onAuthStateChanged(AuthData authData) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mTextInfo.append("Authenticate changed \n");
      }
    });

    if (authData == null) {
      authenticate();
    } else {
      syncFromFirebase();
    }
  }

  public void sync(View view) {
    syncFromFirebase();
  }

  private void syncFromFirebase() {
    mFirebase.child("test/data").addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(final DataSnapshot dataSnapshot) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mTextInfo.setText(GSON.toJson(dataSnapshot.getValue()) + "\n");
          }
        });
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mTextInfo.append("Authenticated \n");
      }
    });
    syncFromFirebase();
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mTextInfo.append("Authenticate error \n");
      }
    });
  }
}
