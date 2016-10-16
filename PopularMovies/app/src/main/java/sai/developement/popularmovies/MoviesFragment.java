package sai.developement.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import sai.developement.popularmovies.adapters.MoviesAdapter;
import sai.developement.popularmovies.data.MoviesContract;
import sai.developement.popularmovies.events.FavoritesChangedEvent;
import sai.developement.popularmovies.sync.MoviesSyncAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    private static final int MOVIES_LOADER_ID = 1;
    private GridView mMoviesGridView;
    private MoviesAdapter mMoviesAdapter;

    private int mSelectedPosition = 0;

    private EventBus mEventBus;

    private String mSortSetting = null;

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
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_movies, container, false);
        mMoviesGridView = (GridView) v.findViewById(R.id.gridview_movies);

        mMoviesAdapter = new MoviesAdapter(getContext(), null, 0);

        mMoviesGridView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mMoviesGridView.setEmptyView(v.findViewById(R.id.empty_view));

        mMoviesGridView.setAdapter(mMoviesAdapter);

        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if(cursor != null) {
                    ((Callback)getActivity()).onItemSelected(MoviesContract.MoviesEntry.
                            buildGetMovie(cursor.getString(Constants.COL_MOVIE_ID)));
                    mSelectedPosition = position;
                }
            }
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(Constants.PARCEL_MOVIE_SELECTED_POSITION)
                && savedInstanceState.containsKey(Constants.PARCEL_MOVIE_SORT_SELECTION)) {
            mSelectedPosition = savedInstanceState.getInt(Constants.PARCEL_MOVIE_SELECTED_POSITION);
            mSortSetting = savedInstanceState.getString(Constants.PARCEL_MOVIE_SORT_SELECTION);
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        String preference = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));

        if(!preference.equalsIgnoreCase(mSortSetting)) {
            MoviesFragment moviesFragment = (MoviesFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
            if(moviesFragment != null) {
                moviesFragment.onSortPreferenceChanged();
            }
            mSortSetting = preference;
        }
    }

    public void onSortPreferenceChanged(){
        mSelectedPosition = GridView.INVALID_POSITION;
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mSelectedPosition != GridView.INVALID_POSITION) {
            outState.putInt(Constants.PARCEL_MOVIE_SELECTED_POSITION, mSelectedPosition);
        }
        outState.putString(Constants.PARCEL_MOVIE_SORT_SELECTION, mSortSetting);
        super.onSaveInstanceState(outState);
    }

    private void updateMovies() {
        String sortPref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));
        if(!sortPref.equalsIgnoreCase(getString(R.string.setting_sort_favorites_value))) {
            MoviesSyncAdapter.syncImmediately(getActivity());
        }
        getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String preference = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));

        Uri moviesSortTypeUri;
        if(preference.equalsIgnoreCase(Constants.SORT_FAVORITES)) {
            moviesSortTypeUri = MoviesContract.MoviesEntry.buildMoviesFavorites();
        }
        else {
            moviesSortTypeUri = MoviesContract.MoviesEntry.buildMoviesSort(preference);
        }

        return new CursorLoader(getContext(),
                moviesSortTypeUri,
                Constants.MOVIE_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mMoviesAdapter.swapCursor((Cursor) data);
        if(((MainActivity)getActivity()).getIsTwoPane() && ((Cursor)data).getCount() > 0) {
            mSelectedPosition = mSelectedPosition != GridView.INVALID_POSITION ? mSelectedPosition : 0;
            mMoviesGridView.performItemClick(mMoviesGridView.getChildAt(mSelectedPosition), mSelectedPosition, mMoviesAdapter.getItemId(mSelectedPosition));
            mMoviesGridView.setItemChecked(mSelectedPosition, true);
            mMoviesGridView.smoothScrollToPosition(mSelectedPosition);
        }
        else {
            ((Callback)getActivity()).resetDetails();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public interface Callback {
        void onItemSelected(Uri movieUri);
        void resetDetails();
    }

    @Subscribe
    public void onEvent(FavoritesChangedEvent event) {
        if(getLoaderManager() != null) {
            mSelectedPosition = GridView.INVALID_POSITION;
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        }
    }
}
