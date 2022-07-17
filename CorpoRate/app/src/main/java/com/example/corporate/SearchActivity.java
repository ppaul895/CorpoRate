package com.example.corporate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView searchField;
    private TextView companyName;
    private Button resetButton;
    private RecyclerView results;
    private CollectionReference companyRef = db.collection("Companies");
    private CompanyAdapter adapter;
    private String name, username, email;
    private static final String TAG = "searchActivity";
    private static final String KEY_NAME_CONTENT= "content";
    private static final String KEY_NAME_UID = "uid";
    private static final String KEY_NAME_NAME = "name";
    private static final String KEY_NAME_USERNAME = "username";
    private static final String KEY_NAME_EMAIL = "email";
    public static final String EXTRA_MESSAGE = "com.example.corporate.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.setTitle("Search");

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
        navigationView.setCheckedItem(R.id.nav_search);

        // Hooks
        searchField = findViewById(R.id.searchField2);
        resetButton = findViewById(R.id.resetButton);
        results = findViewById(R.id.searchResults);
        companyName = findViewById(R.id.companyName);

        // Update search field from home page
        Intent intent = getIntent();
        String searchQuery = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        searchField.setText(searchQuery);
        if (searchQuery != null && searchQuery.isEmpty()) {
            updateRecyclerView("");
        }
        else {
            updateRecyclerView(searchQuery);
        }

        // Handles real time search filtering
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateRecyclerView(editable.toString());
            }
        });

        // Handles reset button
        resetButton.setOnClickListener(view -> {
            if (!searchField.getText().toString().isEmpty())
            searchField.setText("");
        });

        // Fetches data from db for add suggestion button
        DocumentReference docRef = db.collection("Users")
                .document((Objects.requireNonNull(auth.getCurrentUser()).getUid()));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        name = Objects.requireNonNull(document.getString("name"));
                        username = Objects.requireNonNull(document.getString("username"));
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

    public void updateRecyclerView(String search) {
        Query query;
        if (search != null && !search.isEmpty()) {
            String formattedSearch = search.substring(0, 1).toUpperCase() + search.substring(1);
            query = companyRef.orderBy("name").startAt(formattedSearch).endAt(formattedSearch + "\uf8ff");
        }
        else {
            query = companyRef.orderBy("name");
        }
        FirestoreRecyclerOptions<Company> options = new FirestoreRecyclerOptions.Builder<Company>()
                .setQuery(query, Company.class)
                .build();
        adapter = new CompanyAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.searchResults);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        // Handles company card clicks
        adapter.setOnItemClickListener(new CompanyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Company company = documentSnapshot.toObject(Company.class);
                Intent intent = new Intent(SearchActivity.this, CompanyActivity.class);
                intent.putExtra(EXTRA_MESSAGE, Objects.requireNonNull(company).getName());
                startActivity(intent);
            }
        });
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

    /** Suggestion Handling */
    public void addSuggestion(View view) {
        EditText suggestionEditText = (EditText)findViewById(R.id.suggestField);
        String suggestionContent = suggestionEditText.getText().toString();

        if (suggestionContent.isEmpty()) {
            Toast.makeText(SearchActivity.this, "You did not write a suggestion!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a suggestion to be added
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put(KEY_NAME_CONTENT, suggestionContent);
        suggestion.put(KEY_NAME_UID, Objects.requireNonNull(auth.getCurrentUser()).getUid());
        suggestion.put(KEY_NAME_NAME, name);
        suggestion.put(KEY_NAME_USERNAME, username);
        suggestion.put(KEY_NAME_EMAIL, email);

        // Add suggestion to the database
        db.collection("Suggestions").document().set(suggestion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SearchActivity.this, "Suggestion submitted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SearchActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });

        // Clear the user's suggestion text from the EditText field
        suggestionEditText.getText().clear();
    }

    /** Drawer Navigation Handling */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(SearchActivity.this, MainActivity.class));
                break;
            case R.id.nav_search:
                break;
            case R.id.nav_about:
                startActivity(new Intent(SearchActivity.this, AboutActivity.class));
                break;
            case R.id.nav_profile:
                startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SearchActivity.this, LoginActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}