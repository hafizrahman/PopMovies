package com.example.android.popmovies.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.android.popmovies.Const;
import com.example.android.popmovies.model.Movie;
import com.example.android.popmovies.model.Review;
import com.example.android.popmovies.model.Trailer;

@Database(entities = {Movie.class, Trailer.class, Review.class}, version = 1, exportSchema =  false)
public abstract class MovieRoomDatabase extends RoomDatabase {
    public abstract MovieDAO movieDAO();
    public abstract TrailerDAO trailerDAO();
    public abstract ReviewDAO reviewDAO();

    // Singleton setup, because we only want one instance of DB opened at the same time
    private static MovieRoomDatabase INSTANCE;
    public static MovieRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (MovieRoomDatabase.class) {
                if(INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MovieRoomDatabase.class,
                            Const.DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        else {
            Log.d(Const.APP_TAG, "Database already made");

        }
        return INSTANCE;
    }
}