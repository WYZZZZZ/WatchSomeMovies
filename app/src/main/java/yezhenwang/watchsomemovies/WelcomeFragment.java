package yezhenwang.watchsomemovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment implements AdapterView.OnItemClickListener{

    private ArrayAdapter mMoviewAdapter;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        String[] movieName = {
                "Movie 1",
                "Movie 2",
                "Movie 3",
                "Movie 4",
                "Movie 5",
                "Movie 6",
                "Movie 7",
        };

        mMoviewAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.my_array_adapter,
                R.id.movieName,
                movieName);

        ListView movieList = (ListView) rootView.findViewById(R.id.movieList);
        movieList.setAdapter(mMoviewAdapter);
        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        return rootView;
    }

    private void updateMovie() {
        fetchMovieInfo movieInfo = new fetchMovieInfo();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = preferences.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_default));
        movieInfo.execute(sorting);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    public class fetchMovieInfo extends AsyncTask<String, Void, ArrayList> {

        private final String LOG_TAG = fetchMovieInfo.class.getSimpleName();

        @Override
        protected ArrayList doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            String myKey = "4acf4cedab6f8c9e9b7a0a000eacf406";

            String moviesURL = "https://api.themoviedb.org/3/movie/" + strings[0] + "?api_key=" + myKey + "&language=en-US";

            try {
                Uri buildUri = Uri.parse(moviesURL).buildUpon().build();
                URL url = new URL(buildUri.toString());

                Log.v(LOG_TAG, "Built URI" + buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private ArrayList getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String OWN_RESULT = "results";
            final String OWN_POSTER = "poster_path";
            final String OWN_OVERVIEW = "overview";
            final String OWN_RELEASE = "release_date";
            final String OWN_NAME = "title";
            final String OWN_VOTE = "vote_average";

            ArrayList<DownloadResult> resultsArrayList = new ArrayList<DownloadResult>();

            String posterStatic = "http://image.tmdb.org/t/p/w185/";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultsArray = movieJson.getJSONArray(OWN_RESULT);

            for (int i = 0; i <resultsArray.length(); i++) {
                String posterURL;
                String overview;
                String releaseDate;
                String movieName;
                String voteAverage;

                JSONObject movieObject = resultsArray.getJSONObject(i);
                posterURL = posterStatic + movieObject.getString(OWN_POSTER);
                overview = movieObject.getString(OWN_OVERVIEW);
                releaseDate = movieObject.getString(OWN_RELEASE);
                movieName = movieObject.getString(OWN_NAME);
                voteAverage = "Avg. Rating: " + movieObject.getString(OWN_VOTE);

                DownloadResult downloadResult = new DownloadResult(posterURL, overview, releaseDate, movieName, voteAverage);
                resultsArrayList.add(downloadResult);
            }

            return resultsArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            drawListView(arrayList);

        }
    }

    ArrayList<DownloadResult> results;

    public void drawListView(ArrayList<DownloadResult> downloadResults) {
        results = new ArrayList<DownloadResult>();
        ListView movieList = (ListView) getView().findViewById(R.id.movieList);
        results = downloadResults;
        ResultAdapter adapter = new ResultAdapter(getActivity(), downloadResults);
        movieList.setAdapter(adapter);
        movieList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DownloadResult downloadResult = results.get(i);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("result", downloadResult);
        startActivity(intent);
    }
}
