package com.example.corporate;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CompanyActivity extends AppCompatActivity implements ReviewAdapter.onEditListener {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView companyLogo;
    private TextView companyName;
    private TextView companyLocation;
    private RatingBar avgRating;
    private TextView avgEthics;
    private TextView avgEnvironmental;
    private TextView avgLeadership;
    private TextView avgWageEquality;
    private TextView avgWorkingConditions;
    private TextView totalReviews;
    private LinearLayout ratingLayout;
    private ReviewAdapter adapter;
    private RecyclerView reviewView;
    private List<Review> reviewList;
    private String cName;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button cancelAddReview;
    private Button submitAddReview;
    private RatingBar addEnvironmental;
    private RatingBar addEthics;
    private RatingBar addLeadership;
    private RatingBar addWageEquality;
    private RatingBar addWorkingConditions;
    private EditText addDescription;
    private TextView deleteReview;
    private TextView addReviewTitle;

    private double cAvgRating = 0.0;
    private double cAvgEthics = 0.0;
    private double cAvgEnvironmental = 0.0;
    private double cAvgLeadership = 0.0;
    private double cAvgWageEquality = 0.0;
    private double cAvgWorkingConditions = 0.0;
    private int cTotalReviews = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Company Details");
        setContentView(R.layout.activity_company);

        //hooks
        companyLogo = findViewById(R.id.companyLogo);
        companyName = findViewById(R.id.companyName);
        companyLocation = findViewById(R.id.companyLocation);
        avgRating = findViewById(R.id.overallRating);
        avgEthics = findViewById(R.id.companyEthicsRating);
        avgEnvironmental = findViewById(R.id.companyEnvironmentalRating);
        avgLeadership = findViewById(R.id.companyLeadershipRating);
        avgWageEquality = findViewById(R.id.companyWageEqualityRating);
        avgWorkingConditions = findViewById(R.id.companyWorkingConditionsRating);
        totalReviews = findViewById(R.id.totalCompanyReviews);
        ratingLayout = findViewById(R.id.companyRatingDetails);

        Intent intent = getIntent();
        cName = intent.getStringExtra(SearchActivity.EXTRA_MESSAGE);

        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(this, reviewList, this);

        reviewView = findViewById(R.id.reviewView);
        reviewView.setHasFixedSize(true);
        reviewView.setAdapter(adapter);
        reviewView.setLayoutManager(new LinearLayoutManager(this));

        // Drawer Navigation + Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Show All Company Data
        companyName.setText(cName);

        db.collection("Companies").document(cName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                companyLocation.setText(documentSnapshot.getString("location"));
                Glide.with(companyLogo.getContext()).load(documentSnapshot.getString("logo"))
                        .fitCenter().placeholder(companyLogo.getDrawable()).into(companyLogo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Empty");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reviewList.removeAll(reviewList);
        Task<QuerySnapshot> dataQ;
        {
            dataQ = db.collection("Reviews").whereEqualTo("company", cName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        totalReviews.setText(list.size() + "");
                        for (DocumentSnapshot d : list) {
                            Review r = d.toObject(Review.class);
                            assert r != null;
                            r.setDocID(d.getId());
                            reviewList.add(r);
                        }
                        Collections.sort(reviewList, new Review());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Empty");
                    }
                }
            });
        }
        refreshCompanyRatings();
    }

    //drawer nav handling
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(CompanyActivity.this, MainActivity.class));
                break;
            case R.id.nav_search:
                startActivity(new Intent(CompanyActivity.this, SearchActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(CompanyActivity.this, AboutActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(CompanyActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CompanyActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //expands average reviews card
    public void expandCard(View view) {
        if (ratingLayout.getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(ratingLayout, new AutoTransition());
            ratingLayout.setVisibility(View.VISIBLE);
        } else {
            TransitionManager.beginDelayedTransition(ratingLayout, new AutoTransition());
            ratingLayout.setVisibility(View.GONE);
        }
    }

    //add review popup handling
    public void addReview(View view) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View addReviewPopupView = getLayoutInflater().inflate(R.layout.add_review_popup, null);

        cancelAddReview = (Button) addReviewPopupView.findViewById(R.id.cancelAddReviewButton);
        submitAddReview = (Button) addReviewPopupView.findViewById(R.id.submitAddReviewButton);
        addEnvironmental = (RatingBar) addReviewPopupView.findViewById(R.id.addReviewEnvironmentalInput);
        addEthics = (RatingBar) addReviewPopupView.findViewById(R.id.addReviewEthicsInput);
        addLeadership = (RatingBar) addReviewPopupView.findViewById(R.id.addReviewLeadershipInput);
        addWageEquality = (RatingBar) addReviewPopupView.findViewById(R.id.addReviewWageEqualityInput);
        addWorkingConditions = (RatingBar) addReviewPopupView.findViewById(R.id.addReviewWorkingConditionsInput);
        addDescription = (EditText) addReviewPopupView.findViewById(R.id.addReviewTextInput);
        deleteReview = (TextView) addReviewPopupView.findViewById(R.id.addReviewDeleteClick);
        addReviewTitle = (TextView) addReviewPopupView.findViewById(R.id.addReviewTitle);

        deleteReview.setVisibility(View.GONE);
        addReviewTitle.setText("Add Review");
        submitAddReview.setText("Add");

        dialogBuilder.setView(addReviewPopupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        cancelAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        submitAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addEnvironmental.getRating() == 0 || addEthics.getRating() == 0 || addLeadership.getRating() == 0 || addWageEquality.getRating() == 0 || addWorkingConditions.getRating() == 0)
                    Toast.makeText(CompanyActivity.this, "Rating(s) cannot be zero stars!", Toast.LENGTH_SHORT).show();
                else if (TextUtils.isEmpty(addDescription.getText()))
                    addDescription.setError("Enter a few lines about " + cName);
                else {
                    // Create a new review to be added to the database
                    Map<String, Object> newReview = new HashMap<>();
                    newReview.put("avgEnvironmental", (double) addEnvironmental.getRating());
                    newReview.put("avgEthics", (double) addEthics.getRating());
                    newReview.put("avgLeadership", (double) addLeadership.getRating());
                    newReview.put("avgWageEquality", (double) addWageEquality.getRating());
                    newReview.put("avgWorkingConditions", (double) addWorkingConditions.getRating());

                    //iterate through map and calculate average overall
                    double overallRating = 0.0;
                    for (Object value : newReview.values()) {
                        overallRating += (double) value;
                    }
                    overallRating /= 5.0;
                    newReview.put("avgRating", overallRating);

                    newReview.put("UID", Objects.requireNonNull(auth.getCurrentUser()).getUid());
                    newReview.put("company", cName);
                    newReview.put("numOfLikes", 0);
                    newReview.put("reviewText", addDescription.getText().toString());

                    db.collection("Reviews")
                            .add(newReview)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(v.getContext(), "Review Submitted", Toast.LENGTH_SHORT).show();

                                    //update adapter with new review
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @SuppressLint("NotifyDataSetChanged")
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot newReviewDoc = task.getResult();
                                                if (newReviewDoc != null && newReviewDoc.exists()) {
                                                    Review r = newReviewDoc.toObject(Review.class);
                                                    assert r != null;
                                                    r.setDocID(newReviewDoc.getId());
                                                    reviewList.add(r);
                                                    Collections.sort(reviewList, new Review());
                                                    db.collection("Users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).update("numOfReviews", FieldValue.increment(1));
                                                    refreshCompanyRatings();
                                                    adapter.notifyDataSetChanged();
                                                } else {
                                                    Log.d(TAG, "Document does not exist.");
                                                }
                                            } else {
                                                Log.d(TAG, "Failed to pull from database.", task.getException());
                                            }
                                        }
                                    });

                                    Toast.makeText(v.getContext(), "Review Submitted", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Submitted review with ID" + documentReference.getId());
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), "Error please try again", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "Error adding document", e);
                                    dialog.dismiss();
                                }
                            });
                }
            }
        });
    }

    //edit review popup handling
    @Override
    public void onEditClick(int position) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View editReviewPopupView = getLayoutInflater().inflate(R.layout.add_review_popup, null);

        cancelAddReview = (Button) editReviewPopupView.findViewById(R.id.cancelAddReviewButton);
        submitAddReview = (Button) editReviewPopupView.findViewById(R.id.submitAddReviewButton);
        addEnvironmental = (RatingBar) editReviewPopupView.findViewById(R.id.addReviewEnvironmentalInput);
        addEthics = (RatingBar) editReviewPopupView.findViewById(R.id.addReviewEthicsInput);
        addLeadership = (RatingBar) editReviewPopupView.findViewById(R.id.addReviewLeadershipInput);
        addWageEquality = (RatingBar) editReviewPopupView.findViewById(R.id.addReviewWageEqualityInput);
        addWorkingConditions = (RatingBar) editReviewPopupView.findViewById(R.id.addReviewWorkingConditionsInput);
        addDescription = (EditText) editReviewPopupView.findViewById(R.id.addReviewTextInput);
        deleteReview = (TextView) editReviewPopupView.findViewById(R.id.addReviewDeleteClick);
        addReviewTitle = (TextView) editReviewPopupView.findViewById(R.id.addReviewTitle);

        //set data
        Review thisReview = reviewList.get(position);
        addEnvironmental.setRating((float) thisReview.getAvgEnvironmental());
        addEthics.setRating((float) thisReview.getAvgEthics());
        addLeadership.setRating((float) thisReview.getAvgLeadership());
        addWageEquality.setRating((float) thisReview.getAvgWageEquality());
        addWorkingConditions.setRating((float) thisReview.getAvgWorkingConditions());
        addDescription.setText(thisReview.getReviewText());
        addDescription.setSelection(addDescription.getText().length());
        deleteReview.setVisibility(View.VISIBLE);
        addReviewTitle.setText("Edit Review");
        submitAddReview.setText("Update");

        dialogBuilder.setView(editReviewPopupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        cancelAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        submitAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addEnvironmental.getRating() == 0 || addEthics.getRating() == 0 || addLeadership.getRating() == 0 || addWageEquality.getRating() == 0 || addWorkingConditions.getRating() == 0)
                    Toast.makeText(CompanyActivity.this, "Rating(s) cannot be zero stars!", Toast.LENGTH_SHORT).show();
                else if (TextUtils.isEmpty(addDescription.getText()))
                    addDescription.setError("Enter a few lines about " + cName);
                else {
                    // Set new data
                    // Create a new review to be added to the database
                    Map<String, Object> editedReview = new HashMap<>();
                    editedReview.put("avgEnvironmental", (double) addEnvironmental.getRating());
                    editedReview.put("avgEthics", (double) addEthics.getRating());
                    editedReview.put("avgLeadership", (double) addLeadership.getRating());
                    editedReview.put("avgWageEquality", (double) addWageEquality.getRating());
                    editedReview.put("avgWorkingConditions", (double) addWorkingConditions.getRating());

                    //iterate through map and calculate average overall
                    double overallRating = 0.0;
                    for (Object value : editedReview.values()) {
                        overallRating += (double) value;
                    }
                    overallRating /= 5.0;
                    editedReview.put("avgRating", overallRating);
                    editedReview.put("UID", thisReview.getUID());
                    editedReview.put("company", thisReview.getCompany());
                    editedReview.put("numOfLikes", thisReview.getNumOfLikes());

                    editedReview.put("reviewText", addDescription.getText().toString());

                    double finalOverallRating = overallRating;
                    db.collection("Reviews").document(thisReview.getDocID()).set(editedReview).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(Void unused) {
                            thisReview.setAvgEnvironmental((double) Objects.requireNonNull(editedReview.get("avgEnvironmental")));
                            thisReview.setAvgEthics((double) Objects.requireNonNull(editedReview.get("avgEthics")));
                            thisReview.setAvgLeadership((double) Objects.requireNonNull(editedReview.get("avgLeadership")));
                            thisReview.setAvgWageEquality((double) Objects.requireNonNull(editedReview.get("avgWageEquality")));
                            thisReview.setAvgWorkingConditions((double) Objects.requireNonNull(editedReview.get("avgWorkingConditions")));
                            thisReview.setAvgRating(finalOverallRating);
                            thisReview.setReviewText(Objects.requireNonNull(editedReview.get("reviewText")).toString());
                            Toast.makeText(v.getContext(), "Review Updated!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Review edited with id" + thisReview.getDocID());
                            refreshCompanyRatings();
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), "Error, please try again!", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error editing document", e);
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        deleteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogAlert, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                db.collection("Reviews").document(thisReview.getDocID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onSuccess(Void unused) {
                                        db.collection("Users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).update("numOfReviews", FieldValue.increment(-1));
                                        db.collection("Users").whereArrayContains("likedReviews", thisReview.getDocID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                                        document.getReference().update("likedReviews", FieldValue.arrayRemove(thisReview.getDocID()));
                                                    }
                                                } else {
                                                    Log.d(TAG, "Error updating all users' likedReviews field");
                                                }
                                            }
                                        });
                                        Toast.makeText(v.getContext(), "Review Deleted!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Review deleted");
                                        reviewList.remove(position);
                                        Collections.sort(reviewList, new Review());
                                        refreshCompanyRatings();
                                        adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(v.getContext(), "Error, please try again!", Toast.LENGTH_SHORT).show();
                                        Log.w(TAG, "Error deleting document", e);
                                        dialog.dismiss();
                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //no
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.AlertDialogTheme);
                builder.setMessage("Are you sure you want to delete this review?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    public void refreshCompanyRatings(){
        cAvgRating = 0.0;
        cAvgEthics = 0.0;
        cAvgEnvironmental = 0.0;
        cAvgLeadership = 0.0;
        cAvgWageEquality = 0.0;
        cAvgWorkingConditions = 0.0;
        cTotalReviews = 0;

        Task<QuerySnapshot> dataQ;
        {
            dataQ = db.collection("Reviews").whereEqualTo("company", cName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            Review r = d.toObject(Review.class);
                            assert r != null;
                            cAvgRating += r.getAvgRating();
                            cAvgEthics += r.getAvgEthics();
                            cAvgEnvironmental += r.getAvgEnvironmental();
                            cAvgLeadership += r.getAvgLeadership();
                            cAvgWageEquality += r.getAvgWageEquality();
                            cAvgWorkingConditions += r.getAvgWorkingConditions();
                        }
                        cAvgRating /= list.size();
                        cAvgEthics /= list.size();
                        cAvgEnvironmental /= list.size();
                        cAvgLeadership /= list.size();
                        cAvgWageEquality /= list.size();
                        cAvgWorkingConditions /= list.size();
                        cTotalReviews = list.size();

                        cAvgRating = (double)Math.round(cAvgRating * 100) / 100;
                        cAvgEthics = (double)Math.round(cAvgEthics * 100) / 100;
                        cAvgEnvironmental = (double)Math.round(cAvgEnvironmental * 100) / 100;
                        cAvgLeadership = (double)Math.round(cAvgLeadership * 100) / 100;
                        cAvgWageEquality = (double)Math.round(cAvgWageEquality * 100) / 100;
                        cAvgWorkingConditions = (double)Math.round(cAvgWorkingConditions * 100) / 100;

                        db.collection("Companies").document(cName).update("avgRating", cAvgRating);
                        db.collection("Companies").document(cName).update("avgEthics", cAvgEthics);
                        db.collection("Companies").document(cName).update("avgEnvironmental", cAvgEnvironmental);
                        db.collection("Companies").document(cName).update("avgLeadership", cAvgLeadership);
                        db.collection("Companies").document(cName).update("avgWageEquality", cAvgWageEquality);
                        db.collection("Companies").document(cName).update("avgWorkingConditions", cAvgWorkingConditions);
                        db.collection("Companies").document(cName).update("numOfReviews", cTotalReviews);

                        avgRating.setRating((float)cAvgRating);
                        avgEthics.setText((Double.toString(cAvgEthics)));
                        avgEnvironmental.setText((Double.toString(cAvgEnvironmental)));
                        avgLeadership.setText((Double.toString(cAvgLeadership)));
                        avgWageEquality.setText((Double.toString(cAvgWageEquality)));
                        avgWorkingConditions.setText((Double.toString(cAvgWorkingConditions)));
                        totalReviews.setText(cTotalReviews + "");

                    } else {
                        db.collection("Companies").document(cName).update("avgRating", 0);
                        db.collection("Companies").document(cName).update("avgEthics", 0);
                        db.collection("Companies").document(cName).update("avgEnvironmental", 0);
                        db.collection("Companies").document(cName).update("avgLeadership", 0);
                        db.collection("Companies").document(cName).update("avgWageEquality", 0);
                        db.collection("Companies").document(cName).update("avgWorkingConditions", 0);
                        db.collection("Companies").document(cName).update("numOfReviews", 0);

                        avgRating.setRating((float)0);
                        avgEthics.setText("0");
                        avgEnvironmental.setText("0");
                        avgLeadership.setText("0");
                        avgWageEquality.setText("0");
                        avgWorkingConditions.setText("0");
                        totalReviews.setText("0");

                        Log.d(TAG, "Empty");
                    }
                }
            });
        }
    }
}