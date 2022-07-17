package com.example.corporate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ViewPager slideViewPager;
    private LinearLayout dotsLayout;
    private SliderAdapter sliderAdapter;
    private TextView[] dots;
    private static final String TAG = "aboutActivity";
    private static final String KEY_NAME_CONTENT= "content";
    private static final String KEY_NAME_UID = "uid";
    private static final String KEY_NAME_NAME = "name";
    private static final String KEY_NAME_USERNAME = "username";
    private static final String KEY_NAME_EMAIL = "email";
    private String name, username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        this.setTitle("About");

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
        navigationView.setCheckedItem(R.id.nav_about);

        // Slide Viewpager and Dot Layout
        slideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        dotsLayout = (LinearLayout) findViewById(R.id.dotsLayout);
        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        slideViewPager.addOnPageChangeListener(viewListener);

        // Fetches data from db for add report button
        DocumentReference docRef = db.collection("Users")
                .document((Objects.requireNonNull(auth.getCurrentUser()).getUid()));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        name = document.getString("name");
                        username = document.getString("username");
                        email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                    } else {
                        Log.d(TAG, "User Document does not exist.");
                    }
                }
                else {
                    Log.d(TAG, "Failed to pull from database.", task.getException());
                }
            }
        });
    }

    public void addDotsIndicator(int position) {
        dots = new TextView[3];
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.TransparentLightGray));
            dotsLayout.addView(dots[i]);
        }
        if(dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.Milk));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public void addReport(View view) {
        EditText reportEditText = (EditText)findViewById(R.id.reportField);
        String reportContent = reportEditText.getText().toString();

        if (reportContent.isEmpty()) {
            Toast.makeText(AboutActivity.this, "You did not write an issue!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a report to be added
        Map<String, Object> report = new HashMap<>();
        report.put(KEY_NAME_CONTENT, reportContent);
        report.put(KEY_NAME_UID, Objects.requireNonNull(auth.getCurrentUser()).getUid());
        report.put(KEY_NAME_NAME, name);
        report.put(KEY_NAME_USERNAME, username);
        report.put(KEY_NAME_EMAIL, email);

        // Add report to the database
        db.collection("Reports").document().set(report)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AboutActivity.this, "Report submitted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AboutActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });

        // Clear the user's report text from the EditText field
        reportEditText.getText().clear();
    }

    /** Drawer Navigation Handling */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(AboutActivity.this, MainActivity.class));
                break;
            case R.id.nav_search:
                startActivity(new Intent(AboutActivity.this, SearchActivity.class));
                break;
            case R.id.nav_about:
                break;
            case R.id.nav_profile:
                startActivity(new Intent(AboutActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AboutActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}