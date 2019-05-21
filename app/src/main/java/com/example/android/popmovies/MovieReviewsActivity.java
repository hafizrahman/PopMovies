package com.example.android.popmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.android.popmovies.model.Review;
import com.example.android.popmovies.model.Trailer;
import com.example.android.popmovies.viewmodel.MovieListViewModel;
import com.example.android.popmovies.viewmodel.ReviewListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MovieReviewsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ReviewListViewModel mReviewListViewModel;
    private ReviewListAdapter mReviewListAdapter;
    private LiveData<List<Review>> mCurrentReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // Here we set up the RecyclerView
        mRecyclerView = findViewById(R.id.rv_reviews_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent i = getIntent();
        int movieId = i.getIntExtra(Const.PACKAGE_MOVIE_ID, 0);
        String movieTitle = i.getStringExtra(Const.PACKAGE_MOVIE_TITLE);

        this.setTitle(getString(R.string.activity_title_reviews) + movieTitle);

        // Set up view model
        mReviewListViewModel = ViewModelProviders.of(this).get(ReviewListViewModel.class);

        mCurrentReview = mReviewListViewModel.fetchReviews(movieId);
        // Hook up adapter
        mReviewListAdapter = new ReviewListAdapter(this);
        mRecyclerView.setAdapter(mReviewListAdapter);
        mRecyclerView.setHasFixedSize(true);

        subscribeToData();
    }

    // Create observer to the review LiveData
    public void subscribeToData() {
        mCurrentReview.observe(this, new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                mReviewListAdapter.setReviews(reviews);
            }
        });
    }
}
