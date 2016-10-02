package sai.developement.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import sai.developement.popularmovies.adapters.MoviesAdapter;
import sai.developement.popularmovies.data.MoviesContract;
import sai.developement.popularmovies.models.Movie;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    private static final int MOVIES_LOADER_ID = 1;
    private GridView mMoviesGridView;
    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private ArrayList<Movie> mMoviesList = new ArrayList<>();

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(savedInstanceState != null && savedInstanceState.containsKey(Constants.PARCEL_MOVIES_LIST)) {
            mMoviesList = savedInstanceState.getParcelableArrayList(Constants.PARCEL_MOVIES_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movies, container, false);
        mMoviesGridView = (GridView) v.findViewById(R.id.gridview_movies);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressbar_loading);

        mMoviesAdapter = new MoviesAdapter(getContext(), null, 0);
        mMoviesGridView.setAdapter(mMoviesAdapter);

        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO :: USe cursor data to launch detail activity
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.PARCEL_MOVIES_LIST, mMoviesList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovies() {
        getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        if(mMoviesList.size() == 0) {
            if(isNetworkAvailable()) {
                mProgressBar.setVisibility(View.VISIBLE);
                new MoviesFetchTask()
                        .execute(PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(getString(R.string.str_setting_sort_key),
                                        getString(R.string.setting_sort_def_value)));
            }
        }
    }

    /*
    Code snippet taken from:
    http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoNetworkDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.string_no_network_title))
                .setMessage(getString(R.string.str_no_network))
                .setPositiveButton(getString(R.string.str_try_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateMovies();
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String preference = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));
        String sortType = preference.equalsIgnoreCase(Constants.PREFERNCE_POPULARITY) ? Constants.SORT_POPULARITY : Constants.SORT_TOP_RATED;

        Uri moviesSortTypeUri = MoviesContract.MoviesEntry.buildMoviesSort(sortType);

        return new CursorLoader(getContext(),
                moviesSortTypeUri,
                Constants.MOVIE_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if(data == null) {
            showNoNetworkDialog();
            return;
        }
        mMoviesAdapter.swapCursor((Cursor) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mMoviesAdapter.swapCursor(null);
    }

    class MoviesFetchTask extends AsyncTask<String, Void, List<Movie>> {
        private final String TAG = MoviesFetchTask.class.getSimpleName();
        HttpURLConnection mUrlConnection = null;
        BufferedReader mBufferedReader = null;

        @Override
        protected List<Movie> doInBackground(String... params) {
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

                return getMoviesDataFromJSON(stringBuilder.toString(), params[0]);
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

        private List<Movie> getMoviesDataFromJSON(String moviesJSONString, String sortType) throws JSONException{
            List<Movie> movieList = new ArrayList<>();

            JSONObject moviesJSON = new JSONObject(moviesJSONString);
            JSONArray moviesArray = moviesJSON.getJSONArray(Constants.JSON_RESULTS);

            Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(moviesArray.length());

            for(int i = 0; i< moviesArray.length(); i++) {
                JSONObject movieObject = moviesArray.getJSONObject(i);
                String movieId = movieObject.getString(Constants.JSON_MOVIE_ID);
                final String movieTitle = movieObject.getString(Constants.JSON_TITLE);
                final String moviePlot = movieObject.getString(Constants.JSON_OVERVIEW);
                final double movieRating = movieObject.getDouble(Constants.JSON_RATING);
                final String movieReleaseDate = movieObject.getString(Constants.JSON_RELEASE_DATE);
                final String posterRelativePath = movieObject.getString(Constants.JSON_POSTER_PATH);

                movieList.add(new Movie(movieId, movieTitle, posterRelativePath, moviePlot, movieRating, movieReleaseDate));
                ContentValues movieValues = new ContentValues();
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_PLOT, moviePlot);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING, movieRating);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movieTitle);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_URL, posterRelativePath);
                if(sortType.equalsIgnoreCase(Constants.PREFERNCE_POPULARITY)) {
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_POPULARITY);
                }
                else {
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE, Constants.SORT_TOP_RATED);
                }

                contentValuesVector.add(movieValues);
            }

            //Add to database
            if ( contentValuesVector.size() > 0 ) {
                ContentValues[] valuesArray = new ContentValues[contentValuesVector.size()];
                getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, contentValuesVector.toArray(valuesArray));
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mProgressBar.setVisibility(View.GONE);
            mMoviesList.clear();
            mMoviesList.addAll(movies);
            mMoviesAdapter.notifyDataSetChanged();
        }
    }
}
