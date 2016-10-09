package sai.developement.popularmovies;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import sai.developement.popularmovies.adapters.MoviesAdapter;
import sai.developement.popularmovies.async_tasks.MoviesFetchTask;
import sai.developement.popularmovies.data.MoviesContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks{

    private static final int MOVIES_LOADER_ID = 1;
    private GridView mMoviesGridView;
    private MoviesAdapter mMoviesAdapter;

    private int mSelectedPosition = 0;

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

        if(savedInstanceState != null && savedInstanceState.containsKey(Constants.PARCEL_MOVIE_SELECTED_POSITION)) {
            mSelectedPosition = savedInstanceState.getInt(Constants.PARCEL_MOVIE_SELECTED_POSITION);
        }

        return v;
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);
    }



    private void updateMovies() {
        getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
        String sortPref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));

        if(!sortPref.equalsIgnoreCase(getString(R.string.setting_sort_favorites_value))) {
            if(isNetworkAvailable()) {
                new MoviesFetchTask(getContext())
                        .execute(sortPref);
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
        if(data == null) {
            showNoNetworkDialog();
            return;
        }
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
}
