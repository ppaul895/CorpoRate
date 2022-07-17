package com.example.corporate;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context mCtx;
    private final List<Review> reviewList;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private onEditListener mOnEditListener;

    public ReviewAdapter(Context mCtx, List<Review> reviewList, onEditListener mOnEditListener) {
        this.mCtx = mCtx;
        this.reviewList = reviewList;
        this.mOnEditListener = mOnEditListener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewViewHolder(
                LayoutInflater.from(mCtx).inflate(R.layout.review_card, parent, false), mOnEditListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        //get username
        DocumentReference docRef = db.collection("Users")
                .document(review.getUID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        if (!document.getBoolean("anonymous"))
                            holder.username.setText(document.getString("username"));
                        else
                            holder.username.setText("Anonymous User");
                    } else {
                        Log.d(TAG, "Document does not exist.");
                    }
                } else {
                    Log.d(TAG, "Failed to pull from database.", task.getException());
                }
            }
        });

        holder.reviewDesc.setText(review.getReviewText());
        holder.avgRatingBar.setRating(((float) review.getAvgRating()));
        holder.avgEnvironmental.setText("" + review.getAvgEnvironmental());
        holder.avgEthics.setText("" + review.getAvgEthics());
        holder.avgLeadership.setText("" + review.getAvgLeadership());
        holder.avgWageEquality.setText("" + review.getAvgWageEquality());
        holder.avgWorkingConditions.setText("" + review.getAvgWorkingConditions());
        holder.companyName.setText("" + review.getCompany());
        holder.numOfLikesLabel.setText("" + review.getNumOfLikes());

        //set like button state
        DocumentReference docRef1 = db.collection("Users")
                .document((Objects.requireNonNull(auth.getCurrentUser()).getUid()));
        docRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        List<String> likedReviewsList = (List<String>) document.get("likedReviews");
                        assert likedReviewsList != null;
                        holder.likeButton.setChecked(!likedReviewsList.isEmpty() && likedReviewsList.contains(review.getDocID()));
                    } else {
                        Log.d(TAG, "Document does not exist.");
                    }
                } else {
                    Log.d(TAG, "Failed to pull from database.", task.getException());
                }
            }
        });

        // Edit button visibility handling
        if (mCtx instanceof MainActivity)
            holder.companyName.setVisibility(View.VISIBLE);
        else
            holder.companyName.setVisibility(View.GONE);

        if (review.getUID().equals(auth.getCurrentUser().getUid()))
            holder.editButton.setVisibility(View.VISIBLE);
        else
            holder.editButton.setVisibility(View.GONE);

        // Review card click handling
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.subRatingsTop.getVisibility() == View.GONE) {
                    holder.subRatingsTop.setVisibility(View.VISIBLE);
                    holder.subRatingsBottom.setVisibility(View.VISIBLE);
                    holder.reviewDesc.setMaxLines(10);
                } else {
                    holder.subRatingsTop.setVisibility(View.GONE);
                    holder.subRatingsBottom.setVisibility(View.GONE);
                    holder.reviewDesc.setMaxLines(4);
                }
            }
        });

        // Company name click handling
        holder.companyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, CompanyActivity.class);
                intent.putExtra("com.example.corporate.MESSAGE", review.getCompany());
                mCtx.startActivity(intent);
            }
        });

        // Like button toggle handling
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if like button is toggled on, update number of likes in review document and add review ID to user document
                if (holder.likeButton.isChecked()) {
                    db.collection("Reviews").document((Objects.requireNonNull(review.getDocID())))
                            .update("numOfLikes", FieldValue.increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(view.getContext(), "You liked this review!", Toast.LENGTH_SHORT).show();
                                    Collections.sort(reviewList, new Review());
                                    ReviewAdapter.this.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), "Error, this review no longer exists!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    db.collection("Users").document((Objects.requireNonNull(auth.getCurrentUser()).getUid()))
                            .update("likedReviews", FieldValue.arrayUnion(review.getDocID()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Liked review added to user's likeReviews field");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Error, liked review NOT added to user's likeReviews field");
                                }
                            });
                    review.setNumOfLikes(review.getNumOfLikes() + 1);
                }
                //if like button is toggled off, update number of likes in review document and remove review ID from user document
                else {
                    db.collection("Reviews").document(Objects.requireNonNull(review.getDocID()))
                            .update("numOfLikes", FieldValue.increment(-1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(view.getContext(), "You unliked this review!", Toast.LENGTH_SHORT).show();
                                    Collections.sort(reviewList, new Review());
                                    ReviewAdapter.this.notifyDataSetChanged();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), "Error, this review no longer exists!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    db.collection("Users").document((Objects.requireNonNull(auth.getCurrentUser()).getUid()))
                            .update("likedReviews", FieldValue.arrayRemove(review.getDocID()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Liked review removed from user's likeReviews field");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Error, liked review NOT removed from user's likeReviews field");
                                }
                            });
                    review.setNumOfLikes(review.getNumOfLikes() - 1);
                }
                holder.numOfLikesLabel.setText("" + review.getNumOfLikes());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username, reviewDesc, avgEnvironmental, avgEthics, avgLeadership, avgWageEquality, avgWorkingConditions, editButton, companyName, numOfLikesLabel;
        LinearLayout subRatingsTop, subRatingsBottom;
        RatingBar avgRatingBar;
        ToggleButton likeButton;
        onEditListener onEditListener;

        public ReviewViewHolder(View itemView, onEditListener onEditListener) {
            super(itemView);

            username = itemView.findViewById(R.id.reviewUserName);
            avgRatingBar = itemView.findViewById(R.id.avgRatingBar);
            reviewDesc = itemView.findViewById(R.id.reviewDesc);
            avgEnvironmental = itemView.findViewById(R.id.environmentalRating);
            avgEthics = itemView.findViewById(R.id.ethicsRating);
            avgLeadership = itemView.findViewById(R.id.leadershipRating);
            avgWageEquality = itemView.findViewById(R.id.wageEqualityRating);
            avgWorkingConditions = itemView.findViewById(R.id.workingConditionsRating);
            editButton = itemView.findViewById(R.id.reviewEditClick);
            subRatingsTop = itemView.findViewById(R.id.reviewSubRatingsTop);
            subRatingsBottom = itemView.findViewById(R.id.reviewSubRatingsBottom);
            companyName = itemView.findViewById(R.id.reviewCompanyLabel);
            numOfLikesLabel = itemView.findViewById(R.id.numOfLikesLabel);
            likeButton = itemView.findViewById(R.id.heartLikeButton);
            this.onEditListener = onEditListener;
            editButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onEditListener.onEditClick(getAbsoluteAdapterPosition());
        }

    }

    public interface onEditListener {
        void onEditClick(int position);
    }
}