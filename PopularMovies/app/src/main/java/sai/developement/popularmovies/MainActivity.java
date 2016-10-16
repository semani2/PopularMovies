package sai.developement.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import sai.developement.popularmovies.sync.MoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback{

    private boolean mTwoPane;

    private static final String DETAIL_FRAGMENT_TAG = "MovieDetail_Fragment";

    private String mSortSetting = null;

    private MenuItem mShareMenuItem;

    private ShareActionProvider mShareActionProvider;

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

        MoviesSyncAdapter.initializeSyncAdapter(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if(item.getItemId() == R.id.action_refresh) {
            MoviesSyncAdapter.syncImmediately(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean getIsTwoPane() {
        return mTwoPane;
    }
}
