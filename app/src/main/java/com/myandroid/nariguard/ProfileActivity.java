package com.myandroid.nariguard;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    TextView nameField, phoneField, emailField;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ✅ Initialize Views (TextView now, not EditText)
        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        emailField = findViewById(R.id.emailField);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            loadUserData();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // ===============================
    // LOAD USER DATA FROM FIRESTORE
    // ===============================
    private void loadUserData() {

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");

                        nameField.setText(name != null ? name : "No Name");
                        phoneField.setText(phone != null ? phone : "No Phone");
                        emailField.setText(email != null ? email : "No Email");

                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}