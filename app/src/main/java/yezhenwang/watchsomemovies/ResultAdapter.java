package yezhenwang.watchsomemovies;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Yezhen on 2016/11/26.
 */

public class ResultAdapter extends ArrayAdapter{

    ArrayList<DownloadResult> results;
    Context context;

    public ResultAdapter(Context context, ArrayList<DownloadResult> results) {
        super(context, R.layout.my_array_adapter, results);
        this.results = results;
        this.context = context;
    }

    class ResultsViewHolder {
        TextView movieName;
        TextView movieVote;
        ImageView moviePoster;

        ResultsViewHolder(View view) {
            movieName = (TextView) view.findViewById(R.id.movieName);
            movieVote = (TextView) view.findViewById(R.id.movieVote);
            moviePoster = (ImageView) view.findViewById(R.id.moviePoster);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ResultsViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.my_array_adapter, parent, false);
            holder = new ResultsViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ResultsViewHolder) row.getTag();
        }

        DownloadResult downloadResult = results.get(position);
        holder.movieName.setText(downloadResult.movieName);
        holder.movieVote.setText(downloadResult.voteAverage);
        Picasso.with(context)
                .load(downloadResult.posterPath)
                .into(holder.moviePoster);

        return row;
    }
}
