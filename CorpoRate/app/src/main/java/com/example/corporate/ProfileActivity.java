package com.example.corporate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Button signOutButton;
    private Button updateButton;
    private SwitchCompat anonymousSwitch;
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextInputEditText name, username, email, password;
    private TextView headerName, headerUsername, numofReviews, numOfReviewsLabel;
    private static final String TAG = "profileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.setTitle("Profile");

        // Hooks
        headerName = findViewById(R.id.name);
        headerUsername = findViewById(R.id.username);
        name = findViewById(R.id.profileName);
        username = findViewById(R.id.profileUsername);
        email = findViewById(R.id.profileEmail);
        password = findViewById(R.id.profilePassword);
        numofReviews = findViewById(R.id.numOfReviews);
        numOfReviewsLabel = findViewById(R.id.numOfReviewsLabel);
        anonymousSwitch = findViewById(R.id.anonymousSwitch);

        // Show All User Data
        DocumentReference docRef = db.collection("Users")
                .document((Objects.requireNonNull(auth.getCurrentUser()).getUid()));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        headerName.setText(document.getString("name"));
                        headerUsername.setText(document.getString("username"));
                        name.setText(document.getString("name"));
                        username.setText(document.getString("username"));
                        email.setText(auth.getCurrentUser().getEmail());
                        String passwordLength = new String(new char[Objects.requireNonNull(document.getLong("lengthOfPassword")).intValue()]).replace('\0', ' ');
                        password.setText(passwordLength);
                        numofReviews.setText(Objects.requireNonNull(document.get("numOfReviews")).toString());
                        if (Objects.requireNonNull(document.getLong("numOfReviews")).intValue() == 1) {
                            numOfReviewsLabel.setText("Review");
                        }

                        if(Objects.requireNonNull(document.getBoolean("anonymous"))){
                            anonymousSwitch.setChecked(true);
                        }
                    } else {
                        Log.d(TAG, "Document does not exist.");
                    }
                }
                else {
                    Log.d(TAG, "Failed to pull from database.", task.getException());
                }
            }
        });

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
        navigationView.setCheckedItem(R.id.nav_profile);

        // Anonymous switch handling
        anonymousSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = anonymousSwitch.isChecked();

                db.collection("Users").document((Objects.requireNonNull(auth.getCurrentUser()).getUid()))
                        .update("anonymous", isChecked)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(isChecked)
                                    Toast.makeText(ProfileActivity.this, "You are now anonymous!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ProfileActivity.this, "You are now public", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.toString());
                            }
                        });

            }
        });

        // Update button handling
        updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = Objects.requireNonNull(name.getText()).toString();
                String usernameString = Objects.requireNonNull(username.getText()).toString();
                if (nameString.isEmpty() || usernameString.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Name or Username field(s) cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    db.collection("Users").document((Objects.requireNonNull(auth.getCurrentUser()).getUid()))
                            .update("name", nameString,"username", usernameString)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, e.toString());
                                }
                            });
                    headerName.setText(nameString);
                    headerUsername.setText(usernameString);
                }
            }
        });

        // Sign out button handling
        signOutButton = findViewById(R.id.SignOutButton);
        mAuth = FirebaseAuth.getInstance();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            }
        });
    }

    /** Drawer Navigation Handling */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                break;
            case R.id.nav_search:
                startActivity(new Intent(ProfileActivity.this, SearchActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(ProfileActivity.this, AboutActivity.class));
                break;
            case R.id.nav_profile:
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}