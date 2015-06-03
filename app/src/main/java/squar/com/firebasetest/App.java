package squar.com.firebasetest;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;

import android.app.Application;
import android.util.Log;

/**
 * Created by SILONG on 6/2/15.
 */
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Log.e("App", "setPersistenceEnabled: true");
    if (BuildConfig.DEBUG) {
      Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
    }
    Firebase.getDefaultConfig().setEventTarget(new FirebaseEventTarget());
    Firebase.getDefaultConfig().setPersistenceEnabled(true);
  }
}
