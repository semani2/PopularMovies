package sai.developement.popularmovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import sai.developement.popularmovies.Constants;
import sai.developement.popularmovies.R;
import sai.developement.popularmovies.models.Movie;

/**
 * Created by sai on 9/17/16.
 */

public class MoviesAdapter extends ArrayAdapter<Movie> {
    private Context mContext;
    private List<Movie> mMovieList;

    public MoviesAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
        this.mContext = context;
        this.mMovieList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        MovieViewHolder movieViewHolder;

        if(v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.gridview_item, null);

            movieViewHolder = new MovieViewHolder();
            movieViewHolder.posterImageView = (ImageView) v.findViewById(R.id.imageview_movie_poster);
            v.setTag(movieViewHolder);
        }
        else {
            movieViewHolder = (MovieViewHolder) v.getTag();
        }

        String posterURL = Constants.THUMBNAIL_BASE_URL.concat(mMovieList.get(position).getPosterRelativeUrl());
        Picasso.with(mContext)
                .load(posterURL)
                .into(movieViewHolder.posterImageView);

        return v;
    }

    static class MovieViewHolder {
        ImageView posterImageView;
    }
}
