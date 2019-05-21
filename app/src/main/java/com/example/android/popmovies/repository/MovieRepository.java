package com.example.android.popmovies.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.android.popmovies.BuildConfig;
import com.example.android.popmovies.database.MovieDAO;
import com.example.android.popmovies.database.MovieRoomDatabase;
import com.example.android.popmovies.database.ReviewDAO;
import com.example.android.popmovies.database.TrailerDAO;
import com.example.android.popmovies.model.Review;
import com.example.android.popmovies.model.Trailer;
import com.example.android.popmovies.network.TMDBService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.android.popmovies.model.Movie;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import com.example.android.popmovies.Const;


public class MovieRepository {
    private TMDBService service;
    private final MovieDAO mMovieDAO;
    private final TrailerDAO mTrailerDAO;
    private final ReviewDAO mReviewDAO;
    private List<Movie> mListMovies;
    private List<Trailer> mListTrailers;
    private List<Review> mListReviews;
    private SharedPreferences mSharedPreferences;

    public MovieRepository(Application application) {
        MovieRoomDatabase mMovieRoomDB;

        // Initialize Retrofit for API calls
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Const.TMDB_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(TMDBService.class);

        // Initialize Database
        mMovieRoomDB = MovieRoomDatabase.getDatabase(application);
        mMovieDAO = mMovieRoomDB.movieDAO();
        mTrailerDAO = mMovieRoomDB.trailerDAO();
        mReviewDAO = mMovieRoomDB.reviewDAO();

        // Initialize SharedPreferences
        mSharedPreferences = application.getSharedPreferences(Const.PREFS_FILE, Context.MODE_PRIVATE);
    }

    // The caching system works like this:
    // We use the Room database as a single source of truth, and we want to use it as much as possible
    // and not do any HTTP API call if not needed. The shouldCallAPI() function below determines whether
    // we should do another API call, or not (in which case we will just use data from the database).
    //
    // This shouldCallAPI() method determines it by using the following algorithm:
    // First, we have default intervals/waiting time before we do another HTTP API call, saved in
    // SharedPreference:
    // - Const.PREFS_KEY_INTERVAL_MOVIES
    // - Const.PREFS_KEY_INTERVAL_OTHERS
    // For the main movie list API call, the default interval is set as Const.PREFS_VAL_INTERVAL_MOVIES.
    // For other API calls (trailers and reviews), it is set as Const.PREFS_VAL_INTERVAL_OTHERS.
    // Next, we also save a timestamp of the last time we do any API calls.
    // Finally, we check between the interval between the time now, and the last time the call is done.
    // If it hasn't passed the default interval, then we skip the API call and just pull data from database.
    // Otherwise, we do an API call and update/insert the result to the database.
    // A bit more info is also available in the Const.java file.
    private Boolean shouldCallAPI(String modelName, String modelInfo) {
        Long mCurrentTime = System.currentTimeMillis();
        Long mLastUpdate = 0L;
        String mPrefKey;
        int mInterval;

        switch(modelName) {
            case Const.MODEL_MOVIE:
                mInterval = mSharedPreferences.getInt(Const.PREFS_KEY_INTERVAL_MOVIES, 0);
                if(modelInfo.equals(Const.LIST_TYPE_POPULAR)) { // Save for popular movies
                    mPrefKey = Const.PREFS_KEY_POPULAR_LAST_U;
                }
                else { // Save for top rated movies
                    mPrefKey = Const.PREFS_KEY_TOP_RATED_LAST_U;
                }
                break;
            case Const.MODEL_TRAILER:
                mInterval = mSharedPreferences.getInt(Const.PREFS_KEY_INTERVAL_OTHERS, 0);
                mPrefKey = Const.PREFS_KEY_TRAILERS_LAST_U;
                break;
            case Const.MODEL_REVIEW:
                mInterval = mSharedPreferences.getInt(Const.PREFS_KEY_INTERVAL_OTHERS, 0);
                mPrefKey = Const.PREFS_KEY_REVIEWS_LAST_U;
                break;
            default:
                return false; // This should never happen, but if an unsupported modelName is entered, return immediately.
        }
        mLastUpdate = mSharedPreferences.getLong(mPrefKey, 0L);

        // In case everything is zero, meaning none of the prefs are set yet, let's just
        // do API call. This shouldn't happen in practice, but better be safe.
        if(mLastUpdate == 0 && mInterval == 0) {
            return true;
        }
        // If current interval is bigger than pref_interval minutes, let's call API
        Long checkInterval = mCurrentTime - mLastUpdate;
        if (checkInterval >= mInterval) {
            Log.d(Const.APP_TAG, "We need fresh data, current interval:" + checkInterval/1000 + "s, pref_interval:" + mInterval/1000 + "s");
            return true;
        }
        else {
            Log.d(Const.APP_TAG, "We DO NOT need fresh data, current interval:" + checkInterval/1000 + "s, pref_interval:" + mInterval/1000 + "s");
            return false;
        }
    }

    // Fetches the right list of movies from the database.
    public LiveData<List<Movie>> getListMovies(String type) {
        // Early return here: if it's for favorite movies, we pull data straight from DB
        if(type.equals(Const.LIST_TYPE_FAVORITE)) {
            return mMovieDAO.getFavoriteMovies();
        }

        Boolean mShouldCallAPI = shouldCallAPI(Const.MODEL_MOVIE, type);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        switch(type) {
            case Const.LIST_TYPE_POPULAR:
                if(mShouldCallAPI) {
                    fetchMoviesAndSaveToDB(Const.LIST_TYPE_POPULAR);
                }
                mEditor.putLong(Const.PREFS_KEY_POPULAR_LAST_U, System.currentTimeMillis());
                mEditor.apply();
                return mMovieDAO.getPopularMovies();
            case Const.LIST_TYPE_TOP_RATED:
                if(mShouldCallAPI) {
                    fetchMoviesAndSaveToDB(Const.LIST_TYPE_TOP_RATED);
                }
                mEditor.putLong(Const.PREFS_KEY_TOP_RATED_LAST_U, System.currentTimeMillis());
                mEditor.apply();
                return mMovieDAO.getTopRatedMovies();
            default:
                return mMovieDAO.getPopularMovies();
        }
    }

    // Grab data from API call and save to DB
    private void fetchMoviesAndSaveToDB(String type) {
        final Call<JsonObject> mMovieCall;
        switch(type) {
            case Const.LIST_TYPE_POPULAR:
                mMovieCall = service.getPopularMovies(Const.TMDB_API_KEY);
                break;
            case Const.LIST_TYPE_TOP_RATED:
                mMovieCall = service.getTopRatedMovies(Const.TMDB_API_KEY);
                break;
            default:
                mMovieCall = service.getPopularMovies(Const.TMDB_API_KEY); // Get Popular by default
        }

        mMovieCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject mResponse = response.body();

                // We only care about the "results" array inside the returned JsonObject
                JsonArray mResults = mResponse.getAsJsonArray(Const.JSON_KEY_RESULTS);

                // Next we convert the JsonArray into List<Movie>
                // Ref: https://stackoverflow.com/a/18547661
                Type listType = new TypeToken<List<Movie>>() {}.getType();
                mListMovies = new Gson().fromJson(mResults, listType);

                // What we're doing below is an "UPSERT" action:
                // Insert new movie if it's not in the DB (using insertIfNewAsyncTask),
                // or update a movie's certain fields if it's already in the DB (using updateExistingAsyncTask).
                // We are doing this because we only want to update certain fields in the DB and
                // ESPECIALLY to avoid overwriting the "favorite" field value for movies that are
                // already favorited.
                // More info in the database/MovieDAO.java file.
                for(int i = 0; i < mListMovies.size(); i++) {
                    new insertIfNewAsyncTask(mMovieDAO).execute(mListMovies.get(i));
                    new updateExistingAsyncTask(mMovieDAO).execute(mListMovies.get(i));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(Const.APP_TAG, "NETWORK FAIL");
                t.printStackTrace();
            }
        });
    }
    // Method to insert individual Movie data from API to database
    static class insertIfNewAsyncTask extends AsyncTask<Movie, Void, Void> {
        private MovieDAO mAsyncTaskDAO;
        insertIfNewAsyncTask(MovieDAO dao) {
            mAsyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(final Movie... params) {
            mAsyncTaskDAO.insertIfNew(params[0]);
            return null;
        }
    }
    // Update for movie
    static class updateExistingAsyncTask extends AsyncTask<Movie, Void, Void> {
        private MovieDAO mAsyncTaskDAO;
        updateExistingAsyncTask(MovieDAO dao) { mAsyncTaskDAO = dao; }

        @Override
        protected Void doInBackground(final Movie... params) {
            Movie currentMovie = params[0];
            mAsyncTaskDAO.updateExistingMovie(
                    currentMovie.getVoteCount(),
                    currentMovie.getVoteAverage(),
                    currentMovie.getPopularity(),
                    currentMovie.getPosterPath(),
                    currentMovie.getId()
                    );
            return null;
        }
    }
    // Update for movie, this is used just for updating favorites at the moment
    static class updateMovieAsyncTask extends AsyncTask<Movie, Void, Void> {
        private MovieDAO mAsyncTaskDAO;
        updateMovieAsyncTask(MovieDAO dao) { mAsyncTaskDAO = dao; }

        @Override
        protected Void doInBackground(final Movie... params) {
            mAsyncTaskDAO.update(params[0]);
            return null;
        }
    }
    // Insert for trailer
    static class insertTrailerAsyncTask extends AsyncTask<Trailer, Void, Void> {
        private TrailerDAO mAsyncTaskDAO;
        insertTrailerAsyncTask(TrailerDAO dao) {
            mAsyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(final Trailer... params) {
            mAsyncTaskDAO.insert(params[0]);
            return null;
        }
    }
    // Insert for review
    static class insertReviewAsyncTask extends AsyncTask<Review, Void, Void> {
        private ReviewDAO mAsyncTaskDAO;
        insertReviewAsyncTask(ReviewDAO dao) {
            mAsyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(final Review... params) {
            mAsyncTaskDAO.insert(params[0]);
            return null;
        }
    }
    // Updates and toggle favorite status of a movie
    public void toggleFavorite(Movie movie) {
        if (movie.getFavorite() == true) {
            movie.setFavorite(false);
        } else {
            movie.setFavorite(true);
        }
        new updateMovieAsyncTask(mMovieDAO).execute(movie);
    }
    // Fetches the right list of trailers for a certain movie from the database.
    public LiveData<List<Trailer>> getListTrailers(int movieId) {
        Boolean mShouldCallAPI = shouldCallAPI(Const.MODEL_TRAILER, null);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        if (mShouldCallAPI) {
            fetchTrailersAndSaveToDB(movieId);
            mEditor.putLong(Const.PREFS_KEY_TRAILERS_LAST_U, System.currentTimeMillis());
            mEditor.apply();
        }
        return mTrailerDAO.getTrailersFromMovie(movieId);
    }
    private void fetchTrailersAndSaveToDB(final int movieId) {
        final Call<JsonObject> mTrailersCall;
        mTrailersCall = service.getTrailers(movieId, BuildConfig.API_KEY);
        mTrailersCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Trailer currentTrailer;
                JsonObject mResponse = response.body();

                // We only care about the "results" array inside the returned JsonObject
                JsonArray mResults = mResponse.getAsJsonArray(Const.JSON_KEY_RESULTS);

                // Next we convert the JsonArray into List<Movie>
                // Ref: https://stackoverflow.com/a/18547661
                Type listType = new TypeToken<List<Trailer>>() {}.getType();
                mListTrailers = new Gson().fromJson(mResults, listType);

                for (int i=0; i < mListTrailers.size(); i++) {
                    currentTrailer = mListTrailers.get(i);
                    currentTrailer.setMovieId(movieId);
                    new insertTrailerAsyncTask(mTrailerDAO).execute(currentTrailer);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(Const.APP_TAG, "NETWORK FAIL");
                t.printStackTrace();
            }
        });
    }
    // Fetches the right list of movies for a certain movie from the database.
    public LiveData<List<Review>> getListReviews(int movieId) {
        Boolean mShouldCallAPI = shouldCallAPI(Const.MODEL_REVIEW, null);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        if (mShouldCallAPI) {
            fetchReviewsAndSaveToDB(movieId);
            mEditor.putLong(Const.PREFS_KEY_REVIEWS_LAST_U, System.currentTimeMillis());
            mEditor.apply();
        }
        return mReviewDAO.getReviewsForMovieId(movieId);
    }
    private void fetchReviewsAndSaveToDB(final int movieId) {
        final Call<JsonObject> mCall;
        mCall = service.getReviews(movieId, BuildConfig.API_KEY);

        mCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Review mCurrent;
                JsonObject mResponse = response.body();

                // We only care about the "results" array inside the returned JsonObject
                JsonArray mResults = mResponse.getAsJsonArray(Const.JSON_KEY_RESULTS);

                // Next we convert the JsonArray into List<Movie>
                // Ref: https://stackoverflow.com/a/18547661
                Type listType = new TypeToken<List<Review>>() {}.getType();
                mListReviews = new Gson().fromJson(mResults, listType);

                for (int i=0; i < mListReviews.size(); i++) {
                    mCurrent = mListReviews.get(i);
                    mCurrent.setMovieId(movieId);
                    new insertReviewAsyncTask(mReviewDAO).execute(mCurrent);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(Const.APP_TAG, "NETWORK FAIL");
                t.printStackTrace();
            }
        });
    }
}
