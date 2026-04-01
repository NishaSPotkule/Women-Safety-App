package com.myandroid.nariguard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEt, emailEt, phoneEt, passEt;
    Button registerBtn;
    TextView loginText;

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        nameEt = findViewById(R.id.name);
        emailEt = findViewById(R.id.email);
        phoneEt = findViewById(R.id.phone);
        passEt = findViewById(R.id.password);

        registerBtn = findViewById(R.id.registerBtn);
        loginText = findViewById(R.id.loginText);

        registerBtn.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String name = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String pass = passEt.getText().toString().trim();


        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(pass)) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            Toast.makeText(this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        registerBtn.setEnabled(false);


        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid = task.getResult()
                                .getUser()
                                .getUid();


                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("createdAt", System.currentTimeMillis());


                        firestore.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(this,
                                            "Registration Successful",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent =
                                            new Intent(this, ProfileActivity.class);

                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    registerBtn.setEnabled(true);
                                    Toast.makeText(this,
                                            "Firestore Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    }
                    else {
                        registerBtn.setEnabled(true);
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}