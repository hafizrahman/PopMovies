package com.example.android.popmovies.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.android.popmovies.repository.MovieRepository;
import com.example.android.popmovies.model.Movie;

import java.util.List;


public class MovieListViewModel extends AndroidViewModel {
    private MovieRepository mRepository;
    private final MediatorLiveData<List<Movie>> mListMovieMediator;
    private LiveData<List<Movie>> tempLiveDataListMovie;

    public MovieListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MovieRepository(application);
        mListMovieMediator = new MediatorLiveData<>();
        tempLiveDataListMovie = null;

        // Just for testing purpose; uncomment for entering test data
        //mRepository.enterDummyData();
    }

    public void fetchListMovies(String type) {
        LiveData<List<Movie>> mMoviesFromRepository;

        mMoviesFromRepository = mRepository.getListMovies(type);

        // Here we check if tempLiveDataListMovie has already been assigned from a previous
        // fetchListMovies() call or not.
        // If yes, we need to remove it from the source of the MediatorLiveData,
        // as we have new data to put into it.
        if(tempLiveDataListMovie != null) {
            mListMovieMediator.removeSource(tempLiveDataListMovie);
        }

        // Now that the old data is removed, we assign a new source to it.
        // The observer is used so that once the "movies" LiveData finished getting filled
        // with the data (e.g: once the DB query completes filling it), then we set
        // its value to be the value of the MediatorLiveData
        mListMovieMediator.addSource(mMoviesFromRepository, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                mListMovieMediator.setValue(movies);
            }
        });

        // Here we assign the new data to mLiveDataB, for checking purposes on the next
        // fetchListMovies() call
        tempLiveDataListMovie = mMoviesFromRepository;
    }

    public LiveData<List<Movie>> getLiveDataMovies() {
        return mListMovieMediator;
    }

}
