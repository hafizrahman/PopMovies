package com.example.android.popmovies.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.android.popmovies.model.Movie;
import java.util.List;

@Dao
public interface MovieDAO {
    @Query("SELECT * from popmovies_movies WHERE favorite=1")
    LiveData<List<Movie>> getFavoriteMovies();

    @Query("SELECT * from popmovies_movies ORDER BY popularity DESC LIMIT 20")
    LiveData<List<Movie>> getPopularMovies();

    @Query("SELECT * from popmovies_movies ORDER BY voteAverage DESC LIMIT 20")
    LiveData<List<Movie>> getTopRatedMovies();

    @Update
    void update(Movie movie);

    // The two queries below are used for an "UPSERT" purpose:
    // insert new movie if it's not in the DB (using insertIfNew),
    // or update a movie's certain fields if it's already in the DB (using updateExistingMovie).
    // We are doing this because in case of existing movies, we only want to update
    // certain fields in the DB and ESPECIALLY to avoid altering the "favorite" field's value
    // for movies that are already favorited.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIfNew(Movie movie);

    @Query("UPDATE popmovies_movies SET voteCount=:voteCount, voteAverage=:voteAverage, popularity=:popularity, posterPath=:posterPath WHERE id=:id")
    void updateExistingMovie(int voteCount, Double voteAverage, Double popularity, String posterPath, int id);


}
