package com.myandroid.nariguard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EmergencycontactActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddContact;
    private ContactAdapter adapter;
    private List<Contact> contactList;

    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencycontact);

        recyclerView = findViewById(R.id.recyclerViewContacts);
        fabAddContact = findViewById(R.id.fabAddContact);

        firestore = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        contactList = new ArrayList<>();

        // Updated Adapter to support edit & delete
        adapter = new ContactAdapter(this, contactList, new ContactAdapter.OnContactActionListener() {
            @Override
            public void onDelete(Contact contact) {
                deleteContact(contact);
            }

            @Override
            public void onEdit(Contact contact) {
                // Open dialog with existing contact
                AddContactDialogue dialog = new AddContactDialogue(contact);
                dialog.show(getSupportFragmentManager(), "EditContactDialog");
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadContacts();

        fabAddContact.setOnClickListener(v -> {
            // Open dialog for adding new contact
            AddContactDialogue dialog = new AddContactDialogue();
            dialog.show(getSupportFragmentManager(), "AddContactDialog");
        });
    }

    // Make this public so dialog can refresh
    public void loadContacts() {
        firestore.collection("users")
                .document(userId)
                .collection("emergency_contacts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    contactList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Contact contact = doc.toObject(Contact.class);
                        if (contact != null) {
                            contact.setId(doc.getId()); // Store document ID for edit/delete
                            contactList.add(contact);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load contacts", Toast.LENGTH_SHORT).show());
    }

    private void deleteContact(Contact contact) {
        if (contact.getId() == null) return;

        firestore.collection("Users")
                .document(userId)
                .collection("EmergencyContacts")
                .document(contact.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Contact Deleted", Toast.LENGTH_SHORT).show();
                    loadContacts();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show());
    }
}
