package com.example.android.popmovies.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.popmovies.model.Review;
import com.example.android.popmovies.repository.MovieRepository;

import java.util.List;

public class ReviewListViewModel extends AndroidViewModel {
    private MovieRepository mRepository;

    public ReviewListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MovieRepository(application);
    }

    // TODO: fetch data thru repository
    public LiveData<List<Review>> fetchReviews(int movieId) {
        return mRepository.getListReviews(movieId);
    }
}
