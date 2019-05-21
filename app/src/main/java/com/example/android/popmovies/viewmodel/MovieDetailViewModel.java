package com.example.android.popmovies.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.popmovies.model.Trailer;
import com.example.android.popmovies.repository.MovieRepository;
import com.example.android.popmovies.model.Movie;


import java.util.List;

public class MovieDetailViewModel extends AndroidViewModel {
    private MovieRepository mRepository;

    public MovieDetailViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MovieRepository(application);
    }
    public LiveData<List<Trailer>> getLiveDataTrailer(int movieId) {
        return mRepository.getListTrailers(movieId);
    }
    public void toggleFavorite(Movie movie) {
        mRepository.toggleFavorite(movie);
    }


}
