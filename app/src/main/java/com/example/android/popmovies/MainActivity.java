package com.example.android.popmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.popmovies.model.Movie;
import com.example.android.popmovies.viewmodel.MovieListViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieListAdapter.ListItemClickListener {

    private RecyclerView mRecyclerView;
    private MovieListViewModel mMovieListViewModel;
    private MovieListAdapter mMovieListAdapter;
    List<Movie> mCurrentMovies = new ArrayList<>();
    Context context;
    int mCurrentDisplayMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        // Get saved data in preferences and initialize if needed
        SharedPreferences mSharedPreferences = getSharedPreferences(Const.PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        // init the two intervals
        int mInterval = mSharedPreferences.getInt(Const.PREFS_KEY_INTERVAL_MOVIES, 0);
        if (mInterval == 0) {
            mEditor.putInt(Const.PREFS_KEY_INTERVAL_MOVIES, Const.PREFS_VAL_INTERVAL_MOVIES);
            mEditor.apply(); // asynchronous, but should be OK as it's a small data.
        }
        mInterval = mSharedPreferences.getInt(Const.PREFS_KEY_INTERVAL_OTHERS, 0);
        if (mInterval == 0) {
            mEditor.putInt(Const.PREFS_KEY_INTERVAL_OTHERS, Const.PREFS_VAL_INTERVAL_OTHERS);
            mEditor.apply(); // asynchronous, but should be OK as it's a small data.
        }
        // Init the display mode preference
        // If the key doesn't exist, or it exists but somehow the value is null, we init it.
        if(     (!mSharedPreferences.contains(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE)) ||
                (mSharedPreferences.getString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, null) == null)
        ) {
            mEditor.putString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, Const.LIST_TYPE_POPULAR);
            mEditor.apply();
        }

        // Check which mode is last selected
        String mMode = mSharedPreferences.getString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, Const.LIST_TYPE_POPULAR);

        // Here we set up the RecyclerView
        mRecyclerView = findViewById(R.id.rv_movies_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        // Then set up ViewModel
        mMovieListViewModel = ViewModelProviders.of(this).get(MovieListViewModel.class);
        mMovieListViewModel.fetchListMovies(mMode);

        this.setTitle(getString(R.string.activity_title_popular));
        this.setTitle(getActivityTitle(mMode));

        if(mMode.equals(Const.LIST_TYPE_POPULAR)) {
            mCurrentDisplayMenuItem = R.id.action_popular;
        }
        else if(mMode.equals(Const.LIST_TYPE_TOP_RATED)) {
            mCurrentDisplayMenuItem = R.id.action_rating;
        }
        else if(mMode.equals(Const.LIST_TYPE_FAVORITE)) {
            mCurrentDisplayMenuItem = R.id.action_favorite;
        }

        mMovieListAdapter = new MovieListAdapter(
                this,
                mMovieListViewModel.getLiveDataMovies().getValue(),
                this);
        mRecyclerView.setAdapter(mMovieListAdapter);

        subscribeToData();
        mRecyclerView.setHasFixedSize(true);
    }

    // Create observer to the movies LiveData
    // See https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54
    // and https://gist.github.com/JoseAlcerreca/51cdf692abb2e840bfed12fb3fde4b16#file-myfragment-java
    public void subscribeToData() {
        mMovieListViewModel.getLiveDataMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                mMovieListAdapter.setMovies(movies);
                mCurrentMovies = movies;
            }
        });
    }

    // Add menu to activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // Handle menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If currently selected item is the same as the currently displayed list type,
        // then we don't need to do anything, and can return early.
        if(id == mCurrentDisplayMenuItem) {
            Log.d(Const.APP_TAG, "Skipping loading. id: " + id + " while mCurrentDisplayMenuItem: " + mCurrentDisplayMenuItem);
            return true;
        }

        // Get saved data in preferences and initialize if needed
        SharedPreferences mSharedPreferences = getSharedPreferences(Const.PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        switch (id) {
            case R.id.action_popular:
                mMovieListViewModel.fetchListMovies(Const.LIST_TYPE_POPULAR);
                mEditor.putString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, Const.LIST_TYPE_POPULAR);
                this.setTitle(getActivityTitle(Const.LIST_TYPE_POPULAR));
                mCurrentDisplayMenuItem = R.id.action_popular;
                break;
            case R.id.action_rating:
                mMovieListViewModel.fetchListMovies(Const.LIST_TYPE_TOP_RATED);
                mEditor.putString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, Const.LIST_TYPE_TOP_RATED);
                this.setTitle(getActivityTitle(Const.LIST_TYPE_TOP_RATED));
                mCurrentDisplayMenuItem = R.id.action_rating;
                break;
            case R.id.action_favorite:
                mMovieListViewModel.fetchListMovies(Const.LIST_TYPE_FAVORITE);
                mEditor.putString(Const.PREFS_KEY_MAIN_ACTIVITY_DISPLAY_MODE, Const.LIST_TYPE_FAVORITE);
                this.setTitle(getActivityTitle(Const.LIST_TYPE_FAVORITE));
                mCurrentDisplayMenuItem = R.id.action_favorite;
                break;
            default:
                return true;
        }
        mEditor.apply();

        Log.d(Const.APP_TAG, "id: " + id + " | mCurrentDisplayMenuItem: " + mCurrentDisplayMenuItem);

        return super.onOptionsItemSelected(item);
    }

    // Intent to open MovieDetailActivity when a movie is selected
    @Override
    public void onListItemClick(int clickedItemIndex) {
        Movie currentMovie = mCurrentMovies.get(clickedItemIndex);
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(Const.PACKAGE_MOVIE, currentMovie);
        startActivity(intent);
    }

    private String getActivityTitle(String mode) {
        switch(mode) {
            case Const.LIST_TYPE_POPULAR:
                return getString(R.string.activity_title_popular);
            case Const.LIST_TYPE_TOP_RATED:
                return getString(R.string.activity_title_top_rated);
            case Const.LIST_TYPE_FAVORITE:
                return getString(R.string.activity_title_favorites);
            default:
                return getString(R.string.app_name);
        }
    }
}
