package sai.developement.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.webkit.WebSettings.PluginState.ON;

/**
 * Created by sai on 10/1/16.
 */

public class MoviesDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //SQL Query for creating Movies table
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesContract.MoviesEntry.TABLE_NAME + " (" +

                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_MOVIE_PLOT + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE + " TEXT NOT NULL, " +

                "UNIQUE (" + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + ", "+ MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MoviesContract.FavoritesEntry.TABLE_NAME + " (" +

                MoviesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MoviesContract.FavoritesEntry.COLUMN_MOVIES_KEY + " TEXT NOT NULL, " +

                "FOREIGN KEY (" + MoviesContract.FavoritesEntry.COLUMN_MOVIES_KEY + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME + " (" + MoviesContract.MoviesEntry._ID + "));";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoritesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        onCreate(db);
    }
}
