package sai.developement.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import sai.developement.popularmovies.R;

import static sai.developement.popularmovies.Constants.COL_TRAILER_NAME;

/**
 * Created by sai on 10/2/16.
 */

public class TrailersAdapter extends CursorAdapter {

    public TrailersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.trailers_list_item, null);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TrailerViewHolder viewHolder = (TrailerViewHolder) view.getTag();

        viewHolder.trailerNameTextView.setText(cursor.getString(COL_TRAILER_NAME));
    }

    private static class TrailerViewHolder {
        TextView trailerNameTextView;

        public TrailerViewHolder(View view) {
            trailerNameTextView = (TextView) view.findViewById(R.id.trailerTextView);
        }
    }
}
