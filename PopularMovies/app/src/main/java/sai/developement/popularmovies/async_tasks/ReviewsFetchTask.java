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

/**
 * Created by sai on 10/7/16.
 */

public class ReviewsFetchTask extends AsyncTask<String, Void, Void> {
    private final String TAG = ReviewsFetchTask.class.getSimpleName();
    HttpURLConnection mUrlConnection = null;
    BufferedReader mBufferedReader = null;

    private final Context mContext;

    public ReviewsFetchTask(Context context) {
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
                    .appendPath(Constants.REVIEWS)
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

            getReviewsDataFromJSON(stringBuilder.toString(), movieId);
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

    private void getReviewsDataFromJSON(String jsonString, String movieId) throws JSONException{
        JSONObject reviewsJSON = new JSONObject(jsonString);
        JSONArray reviewsArray = reviewsJSON.getJSONArray(Constants.JSON_RESULTS);

        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(reviewsArray.length());

        for (int i = 0; i < reviewsArray.length(); i++) {
            JSONObject reviewObject = reviewsArray.getJSONObject(i);
            final String reviewId = reviewObject.getString(Constants.JSON_REVIEW_ID);
            final String reviewAuthor = reviewObject.getString(Constants.JSON_REVIEW_AUTHOR);
            final String reviewContent = reviewObject.getString(Constants.JSON_REVIEW_CONTENT);

            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIES_KEY, movieId);
            reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW_ID, reviewId);
            reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW_AUTHOR, reviewAuthor);
            reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW_CONTENT, reviewContent);

            contentValuesVector.add(reviewValues);
        }

        if (contentValuesVector.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[contentValuesVector.size()];
            mContext.getContentResolver().bulkInsert(MoviesContract.ReviewsEntry.CONTENT_URI, contentValuesVector.toArray(valuesArray));
        }
    }
}