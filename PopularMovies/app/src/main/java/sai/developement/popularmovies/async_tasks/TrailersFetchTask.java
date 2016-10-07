package sai.developement.popularmovies.async_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import sai.developement.popularmovies.data.MoviesContract;

import static android.R.attr.id;

/**
 * Created by sai on 10/5/16.
 */

public class TrailersFetchTask extends AsyncTask<String, Void, Void> {
    private final String TAG = TrailersFetchTask.class.getSimpleName();
    HttpURLConnection mUrlConnection = null;
    BufferedReader mBufferedReader = null;

    private final Context mContext;

    public TrailersFetchTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            if (params.length == 0) {
                return null;
            }

            String movieId = params[0];
            Uri uri = Uri.parse(Constants.API_BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(Constants.VIDEOS)
                    .appendQueryParameter(Constants.API_KEY_QUERY_PARAM, APIKeys.MOVIES_DB_KEY)
                    .build();

            URL url = new URL(uri.toString());
            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setRequestMethod(Constants.GET_REQUEST);
            mUrlConnection.connect();

            InputStream inputStream = mUrlConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = mBufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            if (stringBuilder.length() == 0) {
                return null;
            }

            getTrailersDataFromJSON(stringBuilder.toString(), movieId);
            return null;
        }
        catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return null;
        }
        catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            return null;
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

    private void getTrailersDataFromJSON(String jsonString, String movieId) throws JSONException{
        JSONObject trailersJson = new JSONObject(jsonString);
        JSONArray trailersArray = trailersJson.getJSONArray(Constants.JSON_RESULTS);

        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(trailersArray.length());

        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject movieObject = trailersArray.getJSONObject(i);
            final String trailerKey = movieObject.getString(Constants.JSON_TRAILER_KEY);
            final String trailerName = movieObject.getString(Constants.JSON_TRAILER_NAME);

            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.TrailersEntry.COLUMN_MOVIES_KEY, movieId);
            movieValues.put(MoviesContract.TrailersEntry.COLUMN_KEY, trailerKey);
            movieValues.put(MoviesContract.TrailersEntry.COLUMN_NAME, trailerName);

            contentValuesVector.add(movieValues);
        }

        if (contentValuesVector.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[contentValuesVector.size()];
            mContext.getContentResolver().bulkInsert(MoviesContract.TrailersEntry.CONTENT_URI, contentValuesVector.toArray(valuesArray));
        }
    }
}
