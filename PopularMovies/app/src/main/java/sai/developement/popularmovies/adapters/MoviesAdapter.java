package sai.developement.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import sai.developement.popularmovies.Constants;
import sai.developement.popularmovies.R;

/**
 * Created by sai on 9/17/16.
 */

public class MoviesAdapter extends CursorAdapter {
    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.gridview_item, null);
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MovieViewHolder movieViewHolder = (MovieViewHolder) view.getTag();

        String posterURL = Constants.THUMBNAIL_BASE_URL.concat(cursor.getString(Constants.COL_POSTER_URL));
        Picasso.with(context)
                .load(posterURL)
                .into(movieViewHolder.posterImageView);
    }

    private static class MovieViewHolder {
        ImageView posterImageView;

        public MovieViewHolder(View view) {
            posterImageView = (ImageView) view.findViewById(R.id.imageview_movie_poster);
        }
    }
}
