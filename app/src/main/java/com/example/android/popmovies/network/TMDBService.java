package com.example.android.popmovies.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

// This is an interface to be used with Retrofit
public interface TMDBService {

    @GET("popular")
    Call<JsonObject> getPopularMovies(@Query("api_key") String apiKey);

    @GET("top_rated")
    Call<JsonObject> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("{movie_id}/videos")
    Call<JsonObject> getTrailers(@Path("movie_id") int movieId, @Query("api_key") String apiKey);

    @GET("{movie_id}/reviews")
    Call<JsonObject> getReviews(@Path("movie_id") int movieId, @Query("api_key") String apiKey);

}
