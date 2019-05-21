package com.example.android.popmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.android.popmovies.model.Review;

import java.util.List;

@Dao
public interface ReviewDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Review review);

    // Get all reviews for a particular movie
    @Query("SELECT * from popmovies_reviews WHERE movieId = :movieId")
    LiveData<List<Review>> getReviewsForMovieId(Integer movieId);
}