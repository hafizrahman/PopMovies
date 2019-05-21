package com.example.android.popmovies;

// Consts are for constants to be used throughout the app.
// A String type constant, can be defined here if it's a String that doesn't need to be translated
// (for example API keys or URLs). Translatable Strings (usually used in the UI) should go to
// strings.xml file instead.
public final class Const {
    public static final String APP_TAG = "POPMOVIES";
    public static final String TMDB_API_KEY = BuildConfig.API_KEY;
    public static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/movie/";
    public static final String POSTER_FULL_PATH = "https://image.tmdb.org/t/p/w185/";
    public static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    public static final String PREFS_FILE = "com.example.android.popmovies.prefs";
    public static final String PREFS_KEY_INTERVAL_MOVIES = "PREF_INTERVAL_MOVIES";
    public static final String PREFS_KEY_INTERVAL_OTHERS = "PREF_INTERVAL_OTHERS";
    public static final String PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE = "PREF_MAIN_DISPLAY_MODE";

    // Allow fetching new movies data every 30 minutes, in milliseconds.
    // The API return for Popular or Top Rated Movies doesn't seem to change that frequently,
    // so having a bit of a waiting time seems okay and would save some bandwidth.
    public static final int PREFS_VAL_INTERVAL_MOVIES = 30 * 60 * 1000;
    // Allow fetching new trailers/reviews data every 2 seconds, in milliseconds.
    // This is intentionally set as short, because in that short time a user might already jump
    // from one movie detail to another, so we need to allow another API call during that duration.
    public static final int PREFS_VAL_INTERVAL_OTHERS = 2 * 1000;

    public static final String PREFS_KEY_POPULAR_LAST_U = "PREF_POPULAR_LAST_U";
    public static final String PREFS_KEY_TOP_RATED_LAST_U = "PREF_TOP_RATED_LAST_U";
    public static final String PREFS_KEY_REVIEWS_LAST_U = "PREF_REVIEWS_LAST_U";
    public static final String PREFS_KEY_TRAILERS_LAST_U = "PREF_TRAILERS_LAST_U";

    public static final String LIST_TYPE_POPULAR = "POPULAR";
    public static final String LIST_TYPE_TOP_RATED = "TOP_RATED";
    public static final String LIST_TYPE_FAVORITE = "FAVORITE";

    public static final String PACKAGE_MOVIE = "MovieData";
    public static final String PACKAGE_MOVIE_ID = "MovieId";
    public static final String PACKAGE_MOVIE_TITLE = "MovieTitle";

    public static final String DB_NAME = "popmovies_movies3";
    public static final String DB_TABLE_MOVIES = "popmovies_movies";
    public static final String DB_TABLE_TRAILERS = "popmovies_trailers";
    public static final String DB_TABLE_REVIEWS = "popmovies_reviews";

    public static final String MODEL_MOVIE = "MOVIE";
    public static final String MODEL_TRAILER = "TRAILER";
    public static final String MODEL_REVIEW = "REVIEW";

    public static final String JSON_KEY_RESULTS = "results";
}
