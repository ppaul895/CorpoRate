package com.example.corporate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class CompanyAdapter extends FirestoreRecyclerAdapter<Company, CompanyAdapter.CompanyHolder> {
    private OnItemClickListener listener;

    public CompanyAdapter(@NonNull FirestoreRecyclerOptions<Company> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CompanyHolder holder, int position, @NonNull Company model) {
        String numOfReviews;
        if(model.getNumOfReviews() == 1) {
            numOfReviews = model.getNumOfReviews() + " Review";
        }
        else {
            numOfReviews = model.getNumOfReviews() + " Reviews";
        }
        holder.companyName.setText(model.getName());
        holder.companyLocation.setText(model.getLocation());
        holder.companyNumReviews.setText(numOfReviews);
        Glide.with(holder.companyLogo.getContext()).load(model.getLogo())
                .fitCenter().placeholder(holder.companyLogo.getDrawable()).into(holder.companyLogo);
        holder.ratingBar.setRating(model.getAvgRating());
    }

    @NonNull
    @Override
    public CompanyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_card,
                parent, false);
        return new CompanyHolder(v);
    }

    class CompanyHolder extends RecyclerView.ViewHolder {
        TextView companyName, companyLocation, companyNumReviews;
        ImageView companyLogo;
        RatingBar ratingBar;

        public CompanyHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.company_name);
            companyLocation = itemView.findViewById(R.id.company_location);
            companyNumReviews = itemView.findViewById(R.id.company_num_reviews);
            companyLogo = itemView.findViewById(R.id.company_logo);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
