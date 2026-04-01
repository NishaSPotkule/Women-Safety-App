package com.myandroid.nariguard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class SafetyTipsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<SafetyModel> tipsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_tips);

        recyclerView = findViewById(R.id.recyclerSafetyTips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tipsList = new ArrayList<>();

        // Add safety topics with multiple tips
        tipsList.add(new SafetyModel("Be Alert", Arrays.asList(
                "Stay aware of your surroundings at all times.",
                "Avoid using headphones in unsafe areas.",
                "Keep your phone handy for emergencies."
        )));

        tipsList.add(new SafetyModel("Use SOS Feature", Arrays.asList(
                "Press the SOS button immediately if you feel unsafe.",
                "Set up trusted contacts to receive alerts.",
                "Check your GPS settings for accurate location."
        )));

        tipsList.add(new SafetyModel("Travel Safety", Arrays.asList(
                "Avoid isolated places.",
                "Sit near the driver in public transport.",
                "Share your route with family or friends."
        )));

        tipsList.add(new SafetyModel("Emergency Contacts", Arrays.asList(
                "Keep trusted contacts on speed dial.",
                "Update contact list regularly.",
                "Inform contacts about your daily routine."
        )));

        tipsList.add(new SafetyModel("Online Safety", Arrays.asList(
                "Never share OTPs or passwords.",
                "Do not accept friend requests from strangers.",
                "Use strong and unique passwords."
        )));

        tipsList.add(new SafetyModel("Trust Your Instincts", Arrays.asList(
                "If something feels wrong, act immediately.",
                "Avoid overthinking, prioritize safety."
        )));

        tipsList.add(new SafetyModel("Self-Defense Awareness", Arrays.asList(
                "Learn basic self-defense techniques.",
                "Stay calm and focused in danger.",
                "Carry a small safety tool if allowed."
        )));

        SafetyTipsAdapter adapter = new SafetyTipsAdapter(tipsList);
        recyclerView.setAdapter(adapter);
    }
}
