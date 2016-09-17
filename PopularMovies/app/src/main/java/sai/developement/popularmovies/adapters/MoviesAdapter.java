package sai.developement.popularmovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import sai.developement.popularmovies.models.Movie;

/**
 * Created by sai on 9/17/16.
 */

public class MoviesAdapter extends ArrayAdapter<Movie> {
    public MoviesAdapter(Context context, int resource, List<Movie> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
