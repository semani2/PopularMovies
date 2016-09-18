package sai.developement.popularmovies.models;

/**
 * Created by sai on 9/17/16.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model for Movie
 */
public class Movie implements Parcelable{
    private String movieId = null;
    private String movieTitle = null;
    private String posterRelativeUrl = null;
    private String moviePlot = null;
    private double movieRating = 0;
    private String movieReleaseDate = null;

    public Movie(String movieId, String movieTitle, String posterRelativeUrl, String moviePlot, double movieRating, String movieReleaseDate) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterRelativeUrl = posterRelativeUrl;
        this.moviePlot = moviePlot;
        this.movieRating = movieRating;
        this.movieReleaseDate = movieReleaseDate;
    }

    private Movie(Parcel parcel) {
        this.movieId = parcel.readString();
        this.movieTitle = parcel.readString();
        this.posterRelativeUrl = parcel.readString();
        this.moviePlot = parcel.readString();
        this.movieRating = parcel.readDouble();
        this.movieReleaseDate = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieId);
        dest.writeString(movieTitle);
        dest.writeString(posterRelativeUrl);
        dest.writeString(moviePlot);
        dest.writeDouble(movieRating);
        dest.writeString(movieReleaseDate);
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getPosterRelativeUrl() {
        return posterRelativeUrl;
    }

    public String getMoviePlot() {
        return moviePlot;
    }


    public double getMovieRating() {
        return movieRating;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<Movie> CREATOR= new Parcelable.Creator<Movie>(){

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
