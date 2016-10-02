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
    public static final String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p/w500/";
    public static final String API_KEY_QUERY_PARAM = "api_key";
    public static final String GET_REQUEST = "GET";
    public static final int MAX_RATING = 10;

    /* Constants for JSON parsing */
    public static final String JSON_RESULTS = "results";
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_MOVIE_ID = "id";
    public static final String JSON_RELEASE_DATE = "release_date";
    public static final String JSON_OVERVIEW = "overview";
    public static final String JSON_TITLE = "title";
    public static final String JSON_RATING = "vote_average";

    /* Preferences */
    public static final String PREFERNCE_POPULARITY = "popularity";

    /* Parcelable Constants */
    public static final String PARCEL_MOVIES_LIST = "movies";
    public static final String PARCEL_MOVIE = "movie";

    /* SORT ORDER FOR DB */
    public static final String SORT_POPULARITY = "popularity";
    public static final String SORT_TOP_RATED = "top_rated";

    public static final String[] MOVIE_PROJECTION = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_PLOT,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING,
            MoviesContract.MoviesEntry.COLUMN_POSTER_URL,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_PLOT = 3;
    public static final int COL_MOVIE_RATING = 4;
    public static final int COL_POSTER_URL = 5;
    public static final int COL_RELEASE_DATE = 6;



}
