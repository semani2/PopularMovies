package sai.developement.popularmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class MovieDetailsFragment extends Fragment {

    private Movie movie;
    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Bundle data) {
        MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
        movieDetailsFragment.setArguments(data);
        return movieDetailsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(getArguments() != null) {
            movie = getArguments().getParcelable(Constants.PARCEL_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movie_details, container, false);
        initView(v);
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

    private void initView(View v) {
        TextView movieTitleTextView = (TextView) v.findViewById(R.id.textview_movie_title);
        TextView movieReleaseDateTextView = (TextView) v.findViewById(R.id.textview_movie_release_date);
        TextView movieRatingTextView = (TextView) v.findViewById(R.id.textview_movie_rating);
        TextView moviePlotTextView = (TextView) v.findViewById(R.id.textview_movie_plot);
        ImageView movieThumbnailImageView = (ImageView) v.findViewById(R.id.imageview_movie_poster);

        movieTitleTextView.setText(movie.getMovieTitle());
        movieRatingTextView.setText(String.valueOf(movie.getMovieRating()).concat("/").concat(String.valueOf(Constants.MAX_RATING)));
        moviePlotTextView.setText(movie.getMoviePlot());
        Picasso.with(getActivity())
                .load(Constants.THUMBNAIL_BASE_URL.concat(movie.getPosterRelativeUrl()))
                .into(movieThumbnailImageView);
        movieReleaseDateTextView.setText(movie.getMovieReleaseDate());
    }

}
