package sai.developement.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback{

    private boolean mTwoPane;

    private static final String DETAIL_FRAGMENT_TAG = "MovieDetail_Fragment";

    private String mSortSetting = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movies_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movies_detail_container, new MovieDetailsFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.str_setting_sort_key),
                        getString(R.string.setting_sort_def_value));

        if(!preference.equalsIgnoreCase(mSortSetting)) {
            MoviesFragment moviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
            if(moviesFragment != null) {
                moviesFragment.onSortPreferenceChanged();
            }
            mSortSetting = preference;
        }
    }

    @Override
    public void onItemSelected(Uri movieUri) {
        if(mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailsFragment.DETAIL_URI, movieUri);

            MovieDetailsFragment detailsFragment = new MovieDetailsFragment();
            detailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movies_detail_container, detailsFragment, DETAIL_FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        }
        else {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(movieUri);
            startActivity(intent);
        }
    }

    @Override
    public void resetDetails() {
        if(mTwoPane) {
            MovieDetailsFragment detailsFragment = (MovieDetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if(detailsFragment != null) {
                detailsFragment.hideDetailsView();
            }
        }
    }

    public boolean getIsTwoPane() {
        return mTwoPane;
    }
}
