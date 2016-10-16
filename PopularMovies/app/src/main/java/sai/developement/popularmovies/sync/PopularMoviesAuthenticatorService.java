package sai.developement.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by sai on 10/16/16.
 */

public class PopularMoviesAuthenticatorService extends Service {

    private PopularMoviesAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new PopularMoviesAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
