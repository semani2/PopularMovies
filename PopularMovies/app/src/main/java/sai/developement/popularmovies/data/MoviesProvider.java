package sai.developement.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;


/**
 * Created by sai on 10/1/16.
 */

public class MoviesProvider extends ContentProvider {

    private MoviesDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sMoviesFavoriteQueryBuilder;

    static {
        sMoviesFavoriteQueryBuilder = new SQLiteQueryBuilder();
        sMoviesFavoriteQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.FavoritesEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry._ID + " = " +
                        MoviesContract.FavoritesEntry.TABLE_NAME +
                        "." + MoviesContract.FavoritesEntry.COLUMN_MOVIES_KEY);
    }

    private static final String sSortTypeMovieSelection =
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_TYPE + " = ?";

    private static final String sMovieIdSelection =
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";

    static final int MOVIE = 100;
    static final int MOVIE_WITH_SORT = 101;
    static final int MOVIE_FAVORITES = 102;
    static final int MOVIE_ID = 103;
    static final int FAVORITE = 200;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/sort/*", MOVIE_WITH_SORT);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/favorites", MOVIE_FAVORITES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/id/*", MOVIE_ID);

        matcher.addURI(authority, MoviesContract.PATH_FAVORITES, FAVORITE);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch(sUriMatcher.match(uri)) {
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE_FAVORITES:
                retCursor = getFavoriteMovies(projection, sortOrder);
                break;

            case MOVIE_WITH_SORT:
                retCursor = getMoviesBySort(uri, projection, sortOrder);
                break;

            case MOVIE_ID:
                retCursor = getMovieById(uri, projection, sortOrder);
                break;

            case FAVORITE:
                retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                sMovieIdSelection,
                new String[]{movieId},
                null,
                null,
                sortOrder);
    }

    private Cursor getMoviesBySort(Uri uri, String[] projection, String sortOrder) {
        String sortType = MoviesContract.MoviesEntry.getSortSettingFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                sSortTypeMovieSelection,
                new String[]{sortType},
                null,
                null,
                sortOrder);
    }

    private Cursor getFavoriteMovies(String[] projection, String sortOrder) {
        return sMoviesFavoriteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;

            case MOVIE_WITH_SORT:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;

            case MOVIE_FAVORITES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;

            case MOVIE_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;

            case FAVORITE:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown URI :" +uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }

            case FAVORITE: {
                long _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.FavoritesEntry.buildFavoritesUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown URI : "+ uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deletedRows;

        switch (match) {
            case MOVIE:
                deletedRows = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FAVORITE:
                deletedRows = db.delete(MoviesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("unknown URI : " + uri);
        }

        if(deletedRows != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updatedRows;

        switch (match) {
            case MOVIE:
                updatedRows = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case FAVORITE:
                updatedRows = db.update(MoviesContract.FavoritesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("unknown URI : " + uri);
        }

        if(updatedRows != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
