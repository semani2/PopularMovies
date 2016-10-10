package sai.developement.popularmovies;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import sai.developement.popularmovies.async_tasks.ReviewsFetchTask;
import sai.developement.popularmovies.async_tasks.TrailersFetchTask;
import sai.developement.popularmovies.data.MoviesContract;
import sai.developement.popularmovies.events.FavoritesChangedEvent;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int MOVIE_TRAILER_LOADER = 1;
    private static final int MOVIE_FAVORITE_LOADER = 2;
    private static final int MOVIE_REVIEWS_LOADER = 3;

    private ProgressBar mTrailersProgressBar;

    private ProgressBar mReviewsProgressBar;

    private LinearLayout mTrailersListLayout;

    private LinearLayout mReviewsListLayout;

    private RelativeLayout mTrailersLayout;

    private RelativeLayout mReviewsLayout;

    private ShareActionProvider mShareActionProvider;

    private Button mToggleFavButton;

    private Integer mMovieId = null;

    private Boolean isInAddState = null;

    private Uri mUri;

    private ScrollView mDetailsLayout;

    static final String DETAIL_URI = "URI";

    private String mYoutubeShareUrl = null;

    public MovieDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_FAVORITE_LOADER, null, this);
        getLoaderManager().initLoader(MOVIE_REVIEWS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments  = getArguments();
        if(arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        View v =  inflater.inflate(R.layout.fragment_movie_details, container, false);

        mTrailersProgressBar = (ProgressBar) v.findViewById(R.id.trailersProgressBar);
        mTrailersListLayout = (LinearLayout) v.findViewById(R.id.trailersListLayout);
        mReviewsListLayout = (LinearLayout) v.findViewById(R.id.reviewsListLayout);
        mReviewsProgressBar = (ProgressBar) v.findViewById(R.id.reviewsProgressBar);
        mDetailsLayout = (ScrollView) v.findViewById(R.id.movie_details_layout);

        mDetailsLayout.setVisibility(View.VISIBLE);

        return v;
    }

    public void hideDetailsView() {
        mDetailsLayout.setVisibility(View.GONE);
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

        movieTitleTextView.setVisibility(View.VISIBLE);
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

        RelativeLayout trailersLayout = (RelativeLayout) getView().findViewById(R.id.trailersLayout);
        if(data.getCount() == 0) {
            trailersLayout.setVisibility(View.GONE);
        }
        else {
            trailersLayout.setVisibility(View.VISIBLE);
        }

        while(data.moveToNext()) {
            final String trailerName = data.getString(Constants.COL_TRAILER_NAME);
            final String youtubeKey = data.getString(Constants.COL_TRAILER_KEY);

            View view = LayoutInflater.from(getContext()).inflate(R.layout.trailers_view, null);
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

    private void initReviewsView(final Cursor data) {
        if(mReviewsListLayout.getChildCount() > 0) {
            mReviewsListLayout.removeAllViews();
        }

        RelativeLayout reviewsLayout = (RelativeLayout) getView().findViewById(R.id.reviewsLayout);
        if(data.getCount() == 0) {
            reviewsLayout.setVisibility(View.GONE);
        }
        else {
            reviewsLayout.setVisibility(View.VISIBLE);
        }

        while(data.moveToNext()) {
            final String reviewAuthor = data.getString(Constants.COL_REVIEW_AUTHOR);
            final String reviewContent = data.getString(Constants.COL_REVIEW_CONTENT);

            LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.reviews_view, null);
            TextView authorTextView = (TextView) view.findViewById(R.id.authorTextView);
            final TextView contentTextView = (TextView) view.findViewById(R.id.reviewTextView);
            final TextView showMoreTextView = (TextView) view.findViewById(R.id.showMoreTextView);

            authorTextView.setText(reviewAuthor);
            contentTextView.setText(reviewContent);

            ShowMoreClickListener clickListener = new ShowMoreClickListener(contentTextView, showMoreTextView);
            showMoreTextView.setOnClickListener(clickListener);

            mReviewsListLayout.addView(view);
        }

        if(mReviewsProgressBar != null) {
            mReviewsProgressBar.setVisibility(View.GONE);
        }
    }

    private class ShowMoreClickListener implements View.OnClickListener {

        private boolean mShouldExpand;

        private final TextView showMoreTextView;
        private final  TextView contentTextView;

        public ShowMoreClickListener(TextView contentTextView, TextView showMoreTextView) {
            mShouldExpand = true;
            this.contentTextView = contentTextView;
            this.showMoreTextView = showMoreTextView;
        }

        @Override
        public void onClick(View v) {
            if(!mShouldExpand) {
                // Compress the textview and set the text to show less
                ViewGroup.LayoutParams layoutParams = contentTextView.getLayoutParams();
                layoutParams.height = getResources().getDimensionPixelSize(R.dimen.review_textview_def_height);

                contentTextView.setLayoutParams(layoutParams);
                showMoreTextView.setText(getString(R.string.strShowMore));
                mShouldExpand = true;
            }
            else {
                // the opposite
                ViewGroup.LayoutParams layoutParams = contentTextView.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                contentTextView.setLayoutParams(layoutParams);
                showMoreTextView.setText(getString(R.string.strShowLess));
                mShouldExpand = false;
            }
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
                EventBus.getDefault().post(new FavoritesChangedEvent());
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
    public void onResume() {
        super.onResume();
        fetchMovieData();
    }

    private void fetchMovieData() {
        if(null == mUri) {
            return;
        }
        fetchMovieDetails();
        fetchTrailers();
        fetchReviews();
        fetchIsFavorite();
    }

    private void fetchMovieDetails() {
        getLoaderManager().restartLoader(MOVIE_DETAIL_LOADER, null, this);
    }

    private void fetchReviews() {
        getLoaderManager().restartLoader(MOVIE_REVIEWS_LOADER, null, this);
        String movieId = MoviesContract.ReviewsEntry.getMovieIdFromMoviesUri(mUri);
        if(movieId == null) {
            return;
        }

        if(Utils.isNetworkAvailable(getContext())) {
            mReviewsProgressBar.setVisibility(View.VISIBLE);
            new ReviewsFetchTask(getContext())
                    .execute(movieId);
        }
    }

    private void fetchIsFavorite() {
        getLoaderManager().restartLoader(MOVIE_FAVORITE_LOADER, null, this);
    }

    private void fetchTrailers() {
        getLoaderManager().restartLoader(MOVIE_TRAILER_LOADER, null, this);
        String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(mUri);
        if(movieId == null) {
            return;
        }

        if(Utils.isNetworkAvailable(getContext())) {
            mTrailersProgressBar.setVisibility(View.VISIBLE);
            new TrailersFetchTask(getContext())
                    .execute(movieId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null == mUri) {
            return null;
        }

        switch (id) {
            case MOVIE_DETAIL_LOADER: {
                return new CursorLoader(getActivity(),
                        mUri,
                        Constants.MOVIE_PROJECTION,
                        null,
                        null,
                        null);
            }

            case MOVIE_TRAILER_LOADER: {
                String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(mUri);
                return new CursorLoader(getActivity(),
                        MoviesContract.TrailersEntry.buildGetTrailersUri(movieId),
                        Constants.TRAILER_PROJECTION,
                        null,
                        null,
                        null);
            }

            case MOVIE_FAVORITE_LOADER: {
                String movieId = MoviesContract.TrailersEntry.getMovieIdFromMoviesUri(mUri);
                return new CursorLoader(getActivity(),
                        MoviesContract.FavoritesEntry.getMovieIsFavorite(movieId),
                        Constants.MOVIE_IS_FAV_PROJECTION,
                        null,
                        null,
                        null);
            }

            case MOVIE_REVIEWS_LOADER: {
                String movieId = MoviesContract.ReviewsEntry.getMovieIdFromMoviesUri(mUri);
                return new CursorLoader(getActivity(),
                        MoviesContract.ReviewsEntry.buildGetReviewsUri(movieId),
                        Constants.REVIEW_PROJECTION,
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
                if(data.getCount() > 0) {
                    data.moveToFirst();
                    String youtubeKey = data.getString(Constants.COL_TRAILER_KEY);
                    mYoutubeShareUrl = Constants.YOUTUBE_BASE_URL.concat(youtubeKey);
                    getActivity().invalidateOptionsMenu();
                }
                return;

            case MOVIE_FAVORITE_LOADER:
                initFavToggleButton(data, getView());
                return;

            case MOVIE_REVIEWS_LOADER:
                initReviewsView(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_details, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if(mShareActionProvider != null && mYoutubeShareUrl != null) {
            mShareActionProvider.setShareIntent(getYoutubeShareIntent());
        }
        else {
            shareItem.setVisible(false);
        }
    }

    private Intent getYoutubeShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, mYoutubeShareUrl);
        intent.setType("text/plain");
        return intent;
    }
}
