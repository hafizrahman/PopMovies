package com.example.android.popmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.android.popmovies.model.Trailer;

import java.util.List;

@Dao
public interface TrailerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Trailer trailer);

    // Get all trailers from a particular movie
    // only pick "Trailer" (because there are other types like Featurettes, etc)
    // and only those from YouTube.
    @Query("SELECT * from popmovies_trailers WHERE movieId = :movieId AND type = 'Trailer' AND site = 'YouTube' ORDER BY name DESC")
    LiveData<List<Trailer>> getTrailersFromMovie(Integer movieId);
}
