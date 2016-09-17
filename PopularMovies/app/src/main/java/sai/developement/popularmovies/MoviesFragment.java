package sai.developement.popularmovies;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import sai.developement.popularmovies.adapters.MoviesAdapter;
import sai.developement.popularmovies.models.Movie;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment {

    private GridView mMoviesGridView;
    private MoviesAdapter mMoviesAdapter;
    private ProgressBar mProgressBar;
    private ArrayList<Movie> mMoviesList = new ArrayList<>();

    public MoviesFragment() {
        // Required empty public constructor
    }

    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movies, container, false);
        mMoviesGridView = (GridView) v.findViewById(R.id.gridview_movies);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressbar_loading);

        mMoviesAdapter = new MoviesAdapter(getContext(), mMoviesList);
        mMoviesGridView.setAdapter(mMoviesAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            //TODO :: Launch settings activity
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovies() {
        mProgressBar.setVisibility(View.VISIBLE);
        new MoviesFetchTask()
                .execute();
    }

    class MoviesFetchTask extends AsyncTask<Void, Void, List<Movie>> {
        private final String TAG = MoviesFetchTask.class.getSimpleName();
        HttpURLConnection mUrlConnection = null;
        BufferedReader mBufferedReader = null;

        @Override
        protected List<Movie> doInBackground(Void... params) {
            try {
                //TODO:: get shared preferences from settings to get top rated or popular movies, defaulting to popular for now
                Uri uri = Uri.parse(Constants.POPULAR_MOVIES_URL).buildUpon()
                        .appendQueryParameter(Constants.API_KEY_QUERY_PARAM, APIKeys.MOVIES_DB_KEY)
                        .build();

                URL url = new URL(uri.toString());
                mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setRequestMethod(Constants.GET_REQUEST);
                mUrlConnection.connect();

                InputStream inputStream = mUrlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = mBufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                    Log.v(TAG, line);
                }

                if (stringBuffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                return getMoviesDataFromJSON(stringBuffer.toString());
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

        private List<Movie> getMoviesDataFromJSON(String moviesJSONString) throws JSONException{
            List<Movie> movieList = new ArrayList<>();

            JSONObject moviesJSON = new JSONObject(moviesJSONString);
            JSONArray moviesArray = moviesJSON.getJSONArray(Constants.JSON_RESULTS);

            for(int i = 0; i< moviesArray.length(); i++) {
                JSONObject movieObject = moviesArray.getJSONObject(i);
                Movie movie = new Movie();
                movie.setMovieId(movieObject.getString(Constants.JSON_MOVIE_ID));
                movie.setMovieTitle(movieObject.getString(Constants.JSON_TITLE));
                movie.setMoviePlot(movieObject.getString(Constants.JSON_OVERVIEW));
                movie.setMovieRating(movieObject.getDouble(Constants.JSON_RATING));
                movie.setMovieReleaseDate(movieObject.getString(Constants.JSON_RELEASE_DATE));
                movie.setPosterRelativeUrl(movieObject.getString(Constants.JSON_POSTER_PATH));

                movieList.add(movie);
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
