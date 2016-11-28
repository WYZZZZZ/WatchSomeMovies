package yezhenwang.watchsomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_detail, new PlaceholderFragment())
                    .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {
        //private static final String LOG_TAG = PlaceholderFragment.class.getSimpleName();
        String movieName;
        String avgRating;
        String movieSynopsis;
        String moviePoster;
        String movieRelease;

        DownloadResult downloadResult;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            TextView detailName = (TextView) rootView.findViewById(R.id.detail_movie_name);
            TextView detailRating = (TextView) rootView.findViewById(R.id.detail_avg_rating);
            TextView detailSynopsis = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
            TextView detailRelease = (TextView) rootView.findViewById(R.id.detail_release_date);
            ImageView detailPoster = (ImageView) rootView.findViewById(R.id.detail_movie_poster);

            if (intent != null && intent.hasExtra(getString(R.string.pass_value))) {
                downloadResult = getActivity().getIntent().getParcelableExtra(getString(R.string.pass_value));

                //Pass the downloaded data from asynctask class to Strings
                movieName = downloadResult.movieName;
                avgRating = downloadResult.voteAverage;
                movieSynopsis = downloadResult.overview;
                movieRelease = getString(R.string.pass_value) + downloadResult.releaseDate;
                moviePoster = getString(R.string.moviedb_w342) + downloadResult.posterPath;

                //Set the textviews and imageview
                detailName.setText(movieName);
                detailRating.setText(avgRating);
                detailSynopsis.setText(movieSynopsis);
                detailRelease.setText(movieRelease);
                Picasso.with(inflater.getContext())
                        .load(downloadResult.posterPath)
                        .into(detailPoster);
            }

            return rootView;
        }
    }
}
