package com.example.corporate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ReviewAdapter.onEditListener{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView displayName;
    private RecyclerView topThreeCompanies;
    private final CollectionReference companyRef = db.collection("Companies");
    private CompanyAdapter adapter;
    private ReviewAdapter reviewAdapter;
    private RecyclerView myReviewsView;
    private List<Review> myReviewList;
    private static final String TAG = "mainActivity";
    private static final int delayAutoScroll = 4000;
    public static final String EXTRA_MESSAGE = "com.example.corporate.MESSAGE";
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
        setContentView(R.layout.activity_main);
        this.setTitle("Home");

        myReviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, myReviewList, this);

        myReviewsView = findViewById(R.id.myReviewsView);
        myReviewsView.setHasFixedSize(true);
        myReviewsView.setAdapter(reviewAdapter);
        myReviewsView.setLayoutManager(new LinearLayoutManager(this));

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
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        // Displays user's name under welcome banner
        displayName = findViewById(R.id.displayName);
        DocumentReference docRef = db.collection("Users")
                .document((Objects.requireNonNull(auth.getCurrentUser()).getUid()));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                displayName.setText(document.getString("name"));
            }
        });

        // Handles the top three companies RecyclerView
        topThreeCompanies = findViewById(R.id.topThreeRecyclerView);
        setUpRecyclerView();

        // Handles company card clicks
        adapter.setOnItemClickListener(new CompanyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Company company = documentSnapshot.toObject(Company.class);
                Intent intent = new Intent(MainActivity.this, CompanyActivity.class);
                intent.putExtra(EXTRA_MESSAGE, Objects.requireNonNull(company).getName());
                startActivity(intent);
            }
        });
    }

    /** Sets up the Recycler View */
    public void setUpRecyclerView() {
        Query query = companyRef.orderBy("numOfReviews", Query.Direction.DESCENDING).limit(5);
        FirestoreRecyclerOptions<Company> options = new FirestoreRecyclerOptions.Builder<Company>()
                .setQuery(query, Company.class)
                .build();
        adapter = new CompanyAdapter(options);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.topThreeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        //Auto scroll Recycler View
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() < (adapter.getItemCount() - 1)) {

                    linearLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(),
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1);
                }

                else if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == (adapter.getItemCount() - 1)) {

                    linearLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), 0);
                }
            }
        }, 0, delayAutoScroll);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        myReviewList.removeAll(myReviewList);
        Task<QuerySnapshot> dataQ;
        {
            dataQ = db.collection("Reviews").whereEqualTo("UID", Objects.requireNonNull(auth.getCurrentUser()).getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            Review r = d.toObject(Review.class);
                            assert r != null;
                            r.setDocID(d.getId());
                            myReviewList.add(r);
                        }
                        Collections.sort(myReviewList, new Review());
                        reviewAdapter.notifyDataSetChanged();
                    } else{
                        Log.d(TAG, "Empty");
                    }
                }
            });
        }
    }

    /** Called when the user taps the Search button */
    public void openSearchResults(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        EditText searchField = (EditText) findViewById(R.id.searchField);
        String searchQuery = searchField.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, searchQuery);
        startActivity(intent);
    }

    /** Drawer Navigation Handling */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
        Review thisReview = myReviewList.get(position);
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
        String cName = thisReview.getCompany();

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
                    Toast.makeText(v.getContext(), "Rating(s) cannot be zero stars!", Toast.LENGTH_SHORT).show();
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
                            refreshCompanyRatings(position);
                            reviewAdapter.notifyDataSetChanged();
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
                                        refreshCompanyRatings(position);
                                        myReviewList.remove(position);
                                        reviewAdapter.notifyDataSetChanged();
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

    public void refreshCompanyRatings(int position){
        String cName = myReviewList.get(position).getCompany();
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

                    } else {
                        db.collection("Companies").document(cName).update("avgRating", 0);
                        db.collection("Companies").document(cName).update("avgEthics", 0);
                        db.collection("Companies").document(cName).update("avgEnvironmental", 0);
                        db.collection("Companies").document(cName).update("avgLeadership", 0);
                        db.collection("Companies").document(cName).update("avgWageEquality", 0);
                        db.collection("Companies").document(cName).update("avgWorkingConditions", 0);
                        db.collection("Companies").document(cName).update("numOfReviews", 0);

                        Log.d(TAG, "Empty");
                    }
                }
            });
        }
    }
}