package sai.developement.popularmovies.models;

/**
 * Created by sai on 9/17/16.
 */

/**
 * Model for Movie
 */
public class Movie {
    private String movieId = null;
    private String movieTitle = null;
    private String posterRelativeUrl = null;
    private String moviePlot = null;
    private double movieRating = 0;
    private String movieReleaseDate = null;

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getPosterRelativeUrl() {
        return posterRelativeUrl;
    }

    public void setPosterRelativeUrl(String posterRelativeUrl) {
        this.posterRelativeUrl = posterRelativeUrl;
    }

    public String getMoviePlot() {
        return moviePlot;
    }

    public void setMoviePlot(String moviePlot) {
        this.moviePlot = moviePlot;
    }

    public double getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(double movieRating) {
        this.movieRating = movieRating;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }
}
