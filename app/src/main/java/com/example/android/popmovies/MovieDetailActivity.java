package com.example.android.popmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popmovies.model.Movie;
import com.example.android.popmovies.model.Trailer;
import com.example.android.popmovies.viewmodel.MovieDetailViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private MovieDetailViewModel mMovieDetailViewModel;
    private LiveData<List<Trailer>> mLiveDataTrailer;
    Movie currentMovie;
    TextView mMovieDetailsWatchTrailersLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent i = getIntent();
        currentMovie = i.getParcelableExtra(Const.PACKAGE_MOVIE);

        TextView mMovieDetailsRealTitle = findViewById(R.id.tv_movie_details_title_real);
        TextView mMovieDetailsEnglishTitle = findViewById(R.id.tv_movie_details_title);
        TextView mMovieDetailsReleaseDate = findViewById(R.id.tv_movie_details_release_date);
        ImageView mMovieDetailsPoster = findViewById(R.id.iv_movie_details_poster);
        TextView mMovieDetailsOverview = findViewById(R.id.tv_movie_details_overview);
        TextView mMovieDetailsVotes = findViewById(R.id.tv_movie_details_votes);

        mMovieDetailsWatchTrailersLabel = findViewById(R.id.tv_movie_watch_trailers_label);

        mMovieDetailsRealTitle.setText(currentMovie.getOriginalTitle());
        mMovieDetailsEnglishTitle.setText(currentMovie.getTitle());

        String mReleaseDate = getString(R.string.ui_released) + currentMovie.getReleaseDate();
        mMovieDetailsReleaseDate.setText(mReleaseDate);
        mMovieDetailsOverview.setText(currentMovie.getOverview());
        mMovieDetailsVotes.setText(currentMovie.getVoteAverage().toString());

        String movie_icon = getResources().getString(R.string.icon_trailers);
        String mWatchTrailersLabel = movie_icon + getString(R.string.ui_watch_trailers);
        mMovieDetailsWatchTrailersLabel.setText(mWatchTrailersLabel);

        // Hide real title if it's the same as English title, we don't need duplicate text.
        // It's only used, usually, for non-English movies.
        if(currentMovie.getOriginalTitle().equals(currentMovie.getTitle())) {
            mMovieDetailsRealTitle.setText(getString(R.string.ui_viewing));
        }

        Button b = findViewById(R.id.btn_toggle_favorite);
        if(currentMovie.getFavorite() == null) {
            currentMovie.setFavorite(false);
        }
        if(currentMovie.getFavorite()) {
            b.setText(R.string.filled_star);
        }

        // Display poster
        String POSTER_PATH = currentMovie.getPosterPath();
        String url = Const.POSTER_FULL_PATH + POSTER_PATH;
        Picasso.get().load(url).into(mMovieDetailsPoster);

        mMovieDetailViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);

        // Get the trailers for this movie
        mLiveDataTrailer = mMovieDetailViewModel.getLiveDataTrailer(currentMovie.getId());
        subscribeToData();
    }

    // Create observer to the trailer LiveData
    public void subscribeToData() {
        mLiveDataTrailer.observe(this, new Observer<List<Trailer>>() {
                @Override
                public void onChanged(List<Trailer> trailers) {
                    Trailer currentTrailer;

                    // Dynamically add buttons for viewing trailers.
                    LinearLayout layout = findViewById(R.id.ll_trailers_list);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    layout.removeAllViews(); // Clean up views first to remove duplicate buttons

                    // Hide the "WATCH TRAILER" label if there's no trailers
                    if(trailers.size() < 1) {
                        mMovieDetailsWatchTrailersLabel.setVisibility(View.GONE);
                    }
                    else {
                        mMovieDetailsWatchTrailersLabel.setVisibility(View.VISIBLE);
                    }

                    // Now we're dynamically building buttons to allow trailer viewing.
                    for (int i=0; i < trailers.size(); i++) {
                        currentTrailer = trailers.get(i);
                        final String youtubeURL = Const.YOUTUBE_BASE_URL + currentTrailer.getKey();

                        // now we're building buttons.
                        Button btnTrailer = new Button(getApplicationContext());
                        btnTrailer.setText(currentTrailer.getName());
                        btnTrailer.setTransformationMethod(null); // remove uppercasing

                        //add button to the layout
                        layout.addView(btnTrailer, params);

                        btnTrailer.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeURL)));
                            }
                        });
                    }
                }
            });
    }

    // Open new activity to show reviews
    public void openReviews(View view) {
        // Open the MovieReviewsActivity using intent
        Intent intent = new Intent(this, MovieReviewsActivity.class);

        // No need to pass the entire Movie object, as the activity only needs
        // movie id and movie title.
        intent.putExtra(Const.PACKAGE_MOVIE_ID, currentMovie.getId());
        intent.putExtra(Const.PACKAGE_MOVIE_TITLE, currentMovie.getTitle());

        startActivity(intent);
    }

    // Set on/off for favorite button
    public void toggleFavorite(View view) {
        mMovieDetailViewModel.toggleFavorite(currentMovie);

        Button b = (Button) view;
        String buttonText = b.getText().toString();

        if(buttonText.equals(getResources().getString(R.string.empty_star))) {
            b.setText(R.string.filled_star);
        }
        else {
            b.setText(R.string.empty_star);
        }
    }
}
