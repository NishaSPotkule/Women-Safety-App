package com.myandroid.nariguard;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddContactDialogue extends DialogFragment {

    private Contact contactToEdit; // null if adding

    public AddContactDialogue() {} // default constructor for adding

    // Constructor for editing
    public AddContactDialogue(Contact contact) {
        this.contactToEdit = contact;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.activity_add_contact_dialogue, null);

        EditText etName = view.findViewById(R.id.etContactName);
        EditText etNumber = view.findViewById(R.id.etContactNumber);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        builder.setView(view);
        Dialog dialog = builder.create();

        // If editing, pre-fill data
        if (contactToEdit != null) {
            etName.setText(contactToEdit.getName());
            etNumber.setText(contactToEdit.getPhoneNumber().replace("+91", ""));
            btnSave.setText("Update");
        } else {
            btnSave.setText("Save");
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String number = etNumber.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (number.matches("\\d{10}")) {
                number = "+91" + number;
            }

            if (!number.matches("\\+\\d{10,15}")) {
                Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("phoneNumber", number);

            if (contactToEdit == null) {
                // ADD NEW CONTACT
                firestore.collection("users")
                        .document(userId)
                        .collection("emergency_contacts")
                        .add(data)
                        .addOnSuccessListener(doc -> {
                            Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
                            dismiss();
                            reloadContacts();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error saving contact", Toast.LENGTH_SHORT).show());
            } else {
                // EDIT EXISTING CONTACT
                firestore.collection("users")
                        .document(userId)
                        .collection("emergency_contacts")
                        .document(contactToEdit.getId())
                        .update(data)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Contact Updated", Toast.LENGTH_SHORT).show();
                            dismiss();
                            reloadContacts();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error updating contact", Toast.LENGTH_SHORT).show());
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void reloadContacts() {
        if (getActivity() instanceof EmergencycontactActivity) {
            ((EmergencycontactActivity) getActivity()).loadContacts();
        }
    }
}
