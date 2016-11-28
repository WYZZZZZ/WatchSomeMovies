package yezhenwang.watchsomemovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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

        //Enable option menu on the up bar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        List<String> movieName = new ArrayList<>();

        mMoviewAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.my_array_adapter,
                movieName);

        GridView movieList = (GridView) rootView.findViewById(R.id.movieList);
        movieList.setAdapter(mMoviewAdapter);

        return rootView;
    }

    //Update downloader and gridView upon preferences change
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

    //Downloader class downloading data from Json in the background thrad
    public class fetchMovieInfo extends AsyncTask<String, Void, ArrayList> {

        private final String LOG_TAG = fetchMovieInfo.class.getSimpleName();

        //
        @Override
        protected ArrayList doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            //Set movieDB user Key from strings.xml
            String myKey = getString(R.string.my_moviedb_api_key);

            /*Set URL according to the "strings" passed, the URL will either be popularity sorting
              or average rating sorting
             */
            String moviesURL = getString(R.string.moviedb_base_url) +
                    strings[0]
                    + getString(R.string.apikey_keyword)
                    + myKey
                    + getString(R.string.moviedb_language_url);

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

        //Pass data from Json
        private ArrayList getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String OWN_RESULT = "results";
            final String OWN_POSTER = "poster_path";
            final String OWN_OVERVIEW = "overview";
            final String OWN_RELEASE = "release_date";
            final String OWN_NAME = "title";
            final String OWN_VOTE = "vote_average";

            ArrayList<DownloadResult> resultsArrayList = new ArrayList<DownloadResult>();

            //Set the static part for image url
            String posterStatic = getString(R.string.moviedb_w342);

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
                voteAverage = getString(R.string.avg_rating) + movieObject.getString(OWN_VOTE);

                //Pass all useful data to an ArrayList
                DownloadResult downloadResult = new DownloadResult(posterURL, overview, releaseDate, movieName, voteAverage);
                resultsArrayList.add(downloadResult);
            }

            return resultsArrayList;
        }

        //Fill the gridView by the data from ArrayList above
        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            drawGridView(arrayList);

        }
    }

    ArrayList<DownloadResult> results;

    //GridView filling class
    public void drawGridView(ArrayList<DownloadResult> downloadResults) {
        results = new ArrayList<DownloadResult>();
        GridView movieList = (GridView) getView().findViewById(R.id.movieList);

        int index = movieList.getFirstVisiblePosition();

        results = downloadResults;
        ResultAdapter adapter = new ResultAdapter(getActivity(), downloadResults);
        movieList.setAdapter(adapter);
        movieList.setOnItemClickListener(this);

        movieList.setSelection(index);
    }

    //Set on click listener for gridView elements
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DownloadResult downloadResult = results.get(i);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("result", downloadResult);
        startActivity(intent);
    }
}
