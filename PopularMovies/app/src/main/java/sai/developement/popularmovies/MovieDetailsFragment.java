package sai.developement.popularmovies;


import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import sai.developement.popularmovies.models.Movie;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_DETAIL_LOADER = 0;
    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movie_details, container, false);
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

    private void initView(View v, Cursor data) {
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(getActivity().getIntent() == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                getActivity().getIntent().getData(),
                Constants.MOVIE_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        initView(getView(), data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here
    }
}
