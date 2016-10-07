package sai.developement.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import sai.developement.popularmovies.async_tasks.TrailersFetchTask;
import sai.developement.popularmovies.data.MoviesContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int MOVIE_TRAILER_LOADER = 1;
    private static final int MOVIE_FAVORITE_LOADER = 2;

    private ProgressBar mTrailersProgressBar;

    private LinearLayout mTrailersListLayout;

    private Button mToggleFavButton;

    private Integer mMovieId = null;

    private Boolean isInAddState = null;

    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_FAVORITE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movie_details, container, false);

        mTrailersProgressBar = (ProgressBar) v.findViewById(R.id.trailersProgressBar);
        mTrailersListLayout = (LinearLayout) v.findViewById(R.id.trailersListLayout);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details_fragment, menu);
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

    private void initMovieView(View v, Cursor data) {
        if(!data.moveToFirst()) {
            return;
        }

        TextView movieTitleTextView = (TextView) v.findViewById(R.id.textview_movie_title);
        TextView movieReleaseDateTextView = (TextView) v.findViewById(R.id.textview_movie_release_date);
        TextView movieRatingTextView = (TextView) v.findViewById(R.id.textview_movie_rating);
        TextView moviePlotTextView = (TextView) v.findViewById(R.id.textview_movie_plot);
        ImageView movieThumbnailImageView = (ImageView) v.findViewById(R.id.imageview_movie_poster);

        movieTitleTextView.setText(data.getString(Constants.COL_MOVIE_TITLE));
        movieRatingTextView.setText(String.valueOf(data.getDouble(Constants.COL_MOVIE_RATING)).concat("/").concat(String.valueOf(Constants.MAX_RATING)));
        moviePlotTextView.setText(data.getString(Constants.COL_MOVIE_PLOT));
        Picasso.with(getActivity())
                .load(Constants.THUMBNAIL_BASE_URL.concat(data.getString(Constants.COL_POSTER_URL)))
                .into(movieThumbnailImageView);
        movieReleaseDateTextView.setText(data.getString(Constants.COL_RELEASE_DATE));
        mMovieId = data.getInt(Constants.COL_MOVIE_ID);
    }

    private void initTrailersView(final Cursor data) {
        if(mTrailersListLayout.getChildCount() > 0) {
            mTrailersListLayout.removeAllViews();
        }

        while(data.moveToNext()) {
            final String trailerName = data.getString(Constants.COL_TRAILER_NAME);
            final String youtubeKey = data.getString(Constants.COL_TRAILER_KEY);

            View view = LayoutInflater.from(getContext()).inflate(R.layout.trailers_list_item, null);
            TextView trailersTextView = (TextView)view.findViewById(R.id.trailerTextView);
            trailersTextView.setText(trailerName);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    watchTrailer(youtubeKey);
                }
            });

            mTrailersListLayout.addView(view);
        }

        if(mTrailersProgressBar != null) {
            mTrailersProgressBar.setVisibility(View.GONE);
        }
    }

    private void initFavToggleButton(final Cursor cursor, View view) {
        mToggleFavButton = (Button) view.findViewById(R.id.toggleFavoriteButton);

        if(cursor.moveToFirst()) {
            mToggleFavButton.setText(getString(R.string.strRemoveFromFav));
            isInAddState = false;
        }
        else {
            mToggleFavButton.setText(getString(R.string.strMarkAsFav));
            isInAddState = true;
        }

        mToggleFavButton.setVisibility(View.VISIBLE);

        mToggleFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInAddState == null) {
                    return;
                }
                else if(isInAddState) {
                    //Add to favorites tables
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesContract.FavoritesEntry.COLUMN_MOVIES_KEY, mMovieId);
                    getContext().getContentResolver().insert(MoviesContract.FavoritesEntry.CONTENT_URI, contentValues);
                }
                else {
                    // Remove from favorites
                    getContext().getContentResolver().delete(MoviesContract.FavoritesEntry.CONTENT_URI, Constants.favoriteDeleteSelectionArgs, new String[]{String.valueOf(mMovieId)});
                }
                getLoaderManager().restartLoader(MOVIE_FAVORITE_LOADER, null, MovieDetailsFragment.this);
            }
        });
    }

    private void watchTrailer(String youtubeKey) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.YOUTUBE_BASE_URL.concat(youtubeKey)));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchTrailers();
        fetchIsFavorite();
    }

    private void fetchIsFavorite() {
        getLoaderManager().restartLoader(MOVIE_FAVORITE_LOADER, null, this);
    }

    private void fetchTrailers() {
        getLoaderManager().restartLoader(MOVIE_TRAILER_LOADER, null, this);
        String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(getActivity().getIntent().getData());
        if(movieId == null) {
            return;
        }

        if(isNetworkAvailable()) {
            mTrailersProgressBar.setVisibility(View.VISIBLE);
            new TrailersFetchTask(getContext())
                    .execute(movieId);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(getActivity().getIntent() == null) {
            return null;
        }

        switch (id) {
            case MOVIE_DETAIL_LOADER: {
                return new CursorLoader(getActivity(),
                        getActivity().getIntent().getData(),
                        Constants.MOVIE_PROJECTION,
                        null,
                        null,
                        null);
            }

            case MOVIE_TRAILER_LOADER: {
                String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(getActivity().getIntent().getData());
                return new CursorLoader(getActivity(),
                        MoviesContract.TrailersEntry.buildGetTrailersUri(movieId),
                        Constants.TRAILER_PROJECTION,
                        null,
                        null,
                        null);
            }

            case MOVIE_FAVORITE_LOADER: {
                String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(getActivity().getIntent().getData());
                return new CursorLoader(getActivity(),
                        MoviesContract.FavoritesEntry.getMovieIsFavorite(movieId),
                        Constants.MOVIE_IS_FAV_PROJECTION,
                        null,
                        null,
                        null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                initMovieView(getView(), data);
                return;

            case MOVIE_TRAILER_LOADER:
                initTrailersView(data);
                return;

            case MOVIE_FAVORITE_LOADER:
                initFavToggleButton(data, getView());
                return;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here
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

}
