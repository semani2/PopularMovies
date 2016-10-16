package sai.developement.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import sai.developement.popularmovies.APIKeys;
import sai.developement.popularmovies.Constants;
import sai.developement.popularmovies.R;
import sai.developement.popularmovies.data.MoviesContract;

import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * Created by sai on 10/16/16.
 */

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter{

    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Performing Movies Sync");
        HttpURLConnection mUrlConnection = null;
        BufferedReader mBufferedReader = null;

        try {
            String baseURL = null;
            String sortOrder = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(getContext().getString(R.string.str_setting_sort_key),
                            getContext().getString(R.string.setting_sort_def_value));
            if (sortOrder.equalsIgnoreCase(Constants.SORT_POPULARITY)) {
                baseURL = Constants.POPULAR_MOVIES_URL;
            } else if(sortOrder.equalsIgnoreCase(Constants.SORT_TOP_RATED)){
                baseURL = Constants.TOP_RATED_MOVIES_URL;
            }
            else {
                return;
            }

            Uri uri = Uri.parse(baseURL).buildUpon()
                    .appendQueryParameter(Constants.API_KEY_QUERY_PARAM, APIKeys.MOVIES_DB_KEY)
                    .build();

            URL url = new URL(uri.toString());
            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setRequestMethod(Constants.GET_REQUEST);
            mUrlConnection.connect();

            InputStream inputStream = mUrlConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = mBufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            if (stringBuilder.length() == 0) {
                return;
            }

            getMoviesDataFromJSON(stringBuilder.toString(), sortOrder);
            return;
        }
        catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return;
        }
        catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            return;
        }
        finally{
            if (mUrlConnection != null) {
                mUrlConnection.disconnect();
            }
            if (mBufferedReader != null) {
                try {
                    mBufferedReader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getMoviesDataFromJSON(String moviesJSONString, String sortType) throws JSONException {
        JSONObject moviesJSON = new JSONObject(moviesJSONString);
        JSONArray moviesArray = moviesJSON.getJSONArray(Constants.JSON_RESULTS);

        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(moviesArray.length());

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObject = moviesArray.getJSONObject(i);
            String movieId = movieObject.getString(Constants.JSON_MOVIE_ID);
            final String movieTitle = movieObject.getString(Constants.JSON_TITLE);
            final String moviePlot = movieObject.getString(Constants.JSON_OVERVIEW);
            final double movieRating = movieObject.getDouble(Constants.JSON_RATING);
            final String movieReleaseDate = movieObject.getString(Constants.JSON_RELEASE_DATE);
            final String posterRelativePath = movieObject.getString(Constants.JSON_POSTER_PATH);

            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_PLOT, moviePlot);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING, movieRating);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movieTitle);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_URL, posterRelativePath);
            if (sortType.equalsIgnoreCase(Constants.SORT_POPULARITY)) {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_POPULARITY);
            } else {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_TOP_RATED);
            }

            contentValuesVector.add(movieValues);
        }

        //Add to database
        if (contentValuesVector.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[contentValuesVector.size()];
            getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, contentValuesVector.toArray(valuesArray));
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if( null == accountManager.getPassword(newAccount)) {
            if(!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account account, Context context) {
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
