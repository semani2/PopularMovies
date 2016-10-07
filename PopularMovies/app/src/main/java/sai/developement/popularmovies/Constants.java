package sai.developement.popularmovies;

import sai.developement.popularmovies.data.MoviesContract;
import sai.developement.popularmovies.models.Movie;

/**
 * Created by sai on 9/17/16.
 */

public class Constants {

    /* Constants for the MOVIES Api */
    public static final String POPULAR_MOVIES_URL = "http://api.themoviedb.org/3/movie/popular";
    public static final String TOP_RATED_MOVIES_URL = "http://api.themoviedb.org/3/movie/top_rated";
    public static final String API_BASE_URL = "http://api.themoviedb.org/3/movie";
    public static final String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p/w500/";
    public static final String API_KEY_QUERY_PARAM = "api_key";
    public static final String GET_REQUEST = "GET";
    public static final int MAX_RATING = 10;
    public static final String VIDEOS = "videos";
    public static final String REVIEWS = "reviews";

    /* Constants for JSON parsing */
    public static final String JSON_RESULTS = "results";
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_MOVIE_ID = "id";
    public static final String JSON_RELEASE_DATE = "release_date";
    public static final String JSON_OVERVIEW = "overview";
    public static final String JSON_TITLE = "title";
    public static final String JSON_RATING = "vote_average";
    public static final String JSON_TRAILER_KEY = "key";
    public static final String JSON_TRAILER_NAME = "name";
    public static final String JSON_REVIEW_ID = "id";
    public static final String JSON_REVIEW_AUTHOR = "author";
    public static final String JSON_REVIEW_CONTENT = "content";

    /* Constants for Intents */
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    /* Parcelable Constants */
    public static final String PARCEL_MOVIES_LIST = "movies";
    public static final String PARCEL_MOVIE = "movie";

    /* SORT ORDER FOR DB */
    public static final String SORT_POPULARITY = "popularity";
    public static final String SORT_TOP_RATED = "top_rated";
    public static final String SORT_FAVORITES = "favorites";

    public static final String[] MOVIE_PROJECTION = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_PLOT,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING,
            MoviesContract.MoviesEntry.COLUMN_POSTER_URL,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_PLOT = 3;
    public static final int COL_MOVIE_RATING = 4;
    public static final int COL_POSTER_URL = 5;
    public static final int COL_RELEASE_DATE = 6;

    public static final String[] TRAILER_PROJECTION = {
            MoviesContract.TrailersEntry.TABLE_NAME + "." + MoviesContract.TrailersEntry._ID,
            MoviesContract.TrailersEntry.TABLE_NAME + "." + MoviesContract.TrailersEntry.COLUMN_NAME,
            MoviesContract.TrailersEntry.TABLE_NAME + "." + MoviesContract.TrailersEntry.COLUMN_KEY
    };

    public static final int COL_TRAILER_ID = 0;
    public static final int COL_TRAILER_NAME = 1;
    public static final int COL_TRAILER_KEY = 2;

    public static final String[] MOVIE_IS_FAV_PROJECTION = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
    };

    public static final int COL_FAV_MOVIE_ID = 0;

    public static final String favoriteDeleteSelectionArgs =
            MoviesContract.FavoritesEntry.TABLE_NAME + "." + MoviesContract.FavoritesEntry.COLUMN_MOVIES_KEY + " = ?";

    public static final String[] REVIEW_PROJECTION = {
            MoviesContract.ReviewsEntry.TABLE_NAME + "." + MoviesContract.ReviewsEntry.COLUMN_REVIEW_AUTHOR,
            MoviesContract.ReviewsEntry.TABLE_NAME + "." + MoviesContract.ReviewsEntry.COLUMN_REVIEW_CONTENT
    };

    public static final int COL_REVIEW_AUTHOR = 0;
    public static final int COL_REVIEW_CONTENT = 1;
}
