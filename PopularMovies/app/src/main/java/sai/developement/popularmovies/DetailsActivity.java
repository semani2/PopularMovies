package sai.developement.popularmovies;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        data.putParcelable(Constants.PARCEL_MOVIE, getIntent().getParcelableExtra(Constants.PARCEL_MOVIE));
        transaction.replace(R.id.fragment_container, MovieDetailsFragment.newInstance(data));
        transaction.commit();
    }
}
