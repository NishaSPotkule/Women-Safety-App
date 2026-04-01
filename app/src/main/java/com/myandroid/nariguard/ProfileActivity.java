package com.myandroid.nariguard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    TextInputEditText nameField, phoneField, emailField;
    MaterialButton editBtn, saveBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ✅ Initialize Views
        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        emailField = findViewById(R.id.emailField);

        editBtn = findViewById(R.id.editBtn);
        saveBtn = findViewById(R.id.saveBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            loadUserData();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // ✅ Enable Editing
        editBtn.setOnClickListener(v -> enableEditing());

        // ✅ Save Changes
        saveBtn.setOnClickListener(v -> updateProfile());
    }

    // ===============================
    // LOAD USER DATA FROM FIRESTORE
    // ===============================
    private void loadUserData() {

        db.collection("users") // must match RegisterActivity
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");

                        nameField.setText(name);
                        phoneField.setText(phone);
                        emailField.setText(email);

                        // ✅ Disable editing initially
                        nameField.setEnabled(false);
                        phoneField.setEnabled(false);
                        emailField.setEnabled(false);
                        saveBtn.setEnabled(false);

                    } else {
                        Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ===============================
    // ENABLE EDIT MODE
    // ===============================
    private void enableEditing() {
        nameField.setEnabled(true);
        phoneField.setEnabled(true);
        saveBtn.setEnabled(true);
    }

    // ===============================
    // UPDATE FIRESTORE DATA
    // ===============================
    private void updateProfile() {

        String name = nameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        db.collection("users")
                .document(userId)
                .update(
                        "name", name,
                        "phone", phone
                )
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                    nameField.setEnabled(false);
                    phoneField.setEnabled(false);
                    saveBtn.setEnabled(false);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}