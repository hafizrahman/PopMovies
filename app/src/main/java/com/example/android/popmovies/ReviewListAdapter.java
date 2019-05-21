package com.example.android.popmovies;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.popmovies.model.Review;
import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewListViewHolder> {

    private final LayoutInflater mInflater;
    private List<Review> mReviews;
    private Context c;

    public ReviewListAdapter(Context context) {
        c = context;
        mInflater = LayoutInflater.from(context);
    }

    // This will get called by Observer of the LiveData that's created on the MainActivity
    // in order to fill and update the adapter's data, if a change is observed.
    void setReviews(List<Review> reviews) {
        mReviews = reviews;
        this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ReviewListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.review_list, viewGroup, false);
        return new ReviewListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewListViewHolder reviewListViewHolder, int i) {
        Review currentReview = mReviews.get(i);
        reviewListViewHolder.mReviewerName.setText(currentReview.getAuthor());
        reviewListViewHolder.mReviewContent.setText(currentReview.getContent());

        // Add alternating background color for easier reading
        if(i %2 != 1) {
            reviewListViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorWhite));
        }
        else {
            reviewListViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorLightGray));
        }
    }

    @Override
    public int getItemCount() {
        if (mReviews != null)
            return mReviews.size();
        else return 0;
    }

    // View holder
    class ReviewListViewHolder extends RecyclerView.ViewHolder {
        public TextView mReviewerName;
        public TextView mReviewContent;
        private ReviewListViewHolder(View itemView) {
            super(itemView);
            mReviewerName = itemView.findViewById(R.id.tv_review_list_name);
            mReviewContent = itemView.findViewById(R.id.tv_review_list_content);
        }
    }
}
