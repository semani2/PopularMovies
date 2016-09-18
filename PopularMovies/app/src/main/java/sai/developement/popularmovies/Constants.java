package sai.developement.popularmovies;

/**
 * Created by sai on 9/17/16.
 */

public class Constants {

    /* Constants for the MOVIES Api */
    public static final String POPULAR_MOVIES_URL = "http://api.themoviedb.org/3/movie/popular";
    public static final String TOP_RATED_MOVIES_URL = "http://api.themoviedb.org/3/movie/top_rated";
    public static final String THUMBNAIL_BASE_URL = "http://image.tmdb.org/t/p/w500/";
    public static final String API_KEY_QUERY_PARAM = "api_key";
    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";
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

}
