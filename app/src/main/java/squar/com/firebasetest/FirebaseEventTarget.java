package squar.com.firebasetest;

import com.firebase.client.EventTarget;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirebaseEventTarget implements EventTarget {

  Executor mExecutor;

  public FirebaseEventTarget() {
    restart();
  }

  @Override
  public void postEvent(Runnable runnable) {
    Log.e("FirebaseEventTarget", "post event");
    mExecutor.execute(runnable);
  }

  @Override
  public void shutdown() {

  }

  @Override
  public void restart() {
    mExecutor = Executors.newFixedThreadPool(3);
  }
}