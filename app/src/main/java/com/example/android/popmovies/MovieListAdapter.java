package com.example.android.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.example.android.popmovies.model.Movie;
import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListViewHolder> {
    private List<Movie> mMovies;
    private final LayoutInflater mInflater;
    final private ListItemClickListener mOnClickListener;

    public MovieListAdapter(Context context, List<Movie> movies, ListItemClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mMovies = movies;
        mOnClickListener = listener;
    }

    // This will get called by Observer of the LiveData that's created on the MainActivity
    // in order to fill and update the adapter's data, if a change is observed.
    void setMovies(List<Movie> movies){
        mMovies = movies;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.movie_list, viewGroup, false);
        return new MovieListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieListViewHolder movieListViewHolder, int i) {
        Movie currentMovie = mMovies.get(i);
        String FULL_PATH = Const.POSTER_FULL_PATH;
        String POSTER_PATH = currentMovie.getPosterPath();
        String url = FULL_PATH + POSTER_PATH;
        Picasso.get().load(url).into(movieListViewHolder.mPosterView);
    }

    @Override
    public int getItemCount() {
        if (mMovies != null)
            return mMovies.size();
        else return 0;
    }

    // View holder
    class MovieListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mPosterView;
        private  MovieListViewHolder(View itemView) {
            super(itemView);
            mPosterView = itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedItemPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedItemPosition);
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemPosition);
    }
}
