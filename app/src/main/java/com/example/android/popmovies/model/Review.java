package com.example.android.popmovies.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.android.popmovies.Const;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = Const.DB_TABLE_REVIEWS)
public class Review {
    // This value is not available from the JSON result, and instead is used to locally save
    // the movie ID related to the particular Trailer
    private Integer movieId;

    @SerializedName("id")
    @Expose
    @NonNull
    @PrimaryKey
    private String id;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("url")
    @Expose
    private String url;


    // Empty generator
    // Just used to prevent this error:
    // error: Entities and Pojos must have a usable public constructor. You can have an empty constructor or a constructor whose parameters match the fields (by name and type).
    public Review() {
    }

    public Integer getMovieId() {
        return movieId;
    }
    public void setMovieId(Integer movie_id) {
        this.movieId = movie_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}