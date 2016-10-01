package sai.developement.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by sai on 10/1/16.
 */

public class PopularMoviesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
