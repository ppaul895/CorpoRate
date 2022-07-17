package com.example.corporate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "RegisterActivity";
    private EditText editTextName, editTextUsername, editTextEmail, editTextPassword;
    private TextView loginClick;
    private Button button_register;
    private FirebaseAuth mAuth;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        button_register = findViewById(R.id.buttonRegister);
        loginClick = findViewById(R.id.loginClick);
        editTextName = findViewById(R.id.editName);
        editTextUsername = findViewById(R.id.editUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        progress = findViewById(R.id.registerProgress);

        mAuth = FirebaseAuth.getInstance();

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(v);
            }
        });

        loginClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    createAccount(v);
                }
                return false;
            }
        });

    }

    private void createAccount(View view) {
        String name = editTextName.getText().toString();
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        int lengthOfPassword = editTextPassword.getText().toString().length();

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if(TextUtils.isEmpty(name)){
            editTextName.setError("Enter your name");
        }
        else if(TextUtils.isEmpty(username)){
            editTextUsername.setError("Enter your username");
        }
        else if(TextUtils.isEmpty(email)){
            editTextEmail.setError("Enter your email");
        }
        else if(TextUtils.isEmpty(password)){
            editTextPassword.setError("Enter your password");
        }
        else if(password.length()<4){
            editTextPassword.setError("Password must be more than 4 characters");
        }
        else if(!isValidEmail(email)){
            editTextEmail.setError("Enter a valid email");
        }
        else{
            progress.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Create a new user to be added to the database
                                Map<String, Object> newUser = new HashMap<>();
                                //List<String>[] likedReviews = new List[0];
                                String[] likedReviews = new String[0];
                                newUser.put("name", name);
                                newUser.put("username", username);
                                newUser.put("lengthOfPassword", lengthOfPassword);
                                newUser.put("numOfReviews", 0);
                                newUser.put("anonymous", false);
                                newUser.put("likedReviews", Arrays.asList(likedReviews));

                                // Add the new user to the database
                                db.collection("Users").document(user.getUid()).set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "New user added to database");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, e.toString());
                                            }
                                        });
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progress.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private Boolean isValidEmail(CharSequence target){
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}