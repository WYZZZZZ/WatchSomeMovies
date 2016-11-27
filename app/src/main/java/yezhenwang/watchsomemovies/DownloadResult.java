package yezhenwang.watchsomemovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Yezhen on 2016/11/26.
 */

public class DownloadResult implements Parcelable {
    String posterPath;
    String overview;
    String releaseDate;
    String movieName;
    String voteAverage;

    protected DownloadResult(Parcel in) {
        posterPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        movieName = in.readString();
        voteAverage = in.readString();
    }

    public DownloadResult(String posterPath, String overview, String releaseDate, String movieName, String voteAverage) {
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.movieName = movieName;
        this.voteAverage = voteAverage;
    }

    public static final Creator<DownloadResult> CREATOR = new Creator<DownloadResult>() {
        @Override
        public DownloadResult createFromParcel(Parcel in) {
            return new DownloadResult(in);
        }

        @Override
        public DownloadResult[] newArray(int size) {
            return new DownloadResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(posterPath);
        parcel.writeString(overview);
        parcel.writeString(releaseDate);
        parcel.writeString(movieName);
        parcel.writeString(voteAverage);
    }
}
