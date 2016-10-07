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
 * Created by sai on 10/2/16.
 */

public class MoviesFetchTask extends AsyncTask<String, Void, Void> {
    private final String TAG = MoviesFetchTask.class.getSimpleName();
    HttpURLConnection mUrlConnection = null;
    BufferedReader mBufferedReader = null;

    private final Context mContext;

    public MoviesFetchTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            String baseURL;
            if(params == null) {
                baseURL = Constants.POPULAR_MOVIES_URL;
            }
            else {
                String sortOrder = params[0];
                if (sortOrder.equalsIgnoreCase(Constants.PREFERNCE_POPULARITY)) {
                    baseURL = Constants.POPULAR_MOVIES_URL;
                } else {
                    baseURL = Constants.TOP_RATED_MOVIES_URL;
                }
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

            getMoviesDataFromJSON(stringBuilder.toString(), params[0]);
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
            if (sortType.equalsIgnoreCase(Constants.PREFERNCE_POPULARITY)) {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_POPULARITY);
            } else {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_TOP_RATED);
            }

            contentValuesVector.add(movieValues);
        }

        //Add to database
        if (contentValuesVector.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[contentValuesVector.size()];
            mContext.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, contentValuesVector.toArray(valuesArray));
        }
    }
}
