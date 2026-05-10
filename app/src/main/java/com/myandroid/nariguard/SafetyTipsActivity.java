package com.myandroid.nariguard;

import android.graphics.Color;
import android.os.Build;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#090204"));
        }

        recyclerView = findViewById(R.id.recyclerSafetyTips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tipsList = new ArrayList<>();

        tipsList.add(new SafetyModel("Safe Commuting Tips",
                Arrays.asList("Stay in well-lit areas",
                        "Avoid isolated roads",
                        "Share live location",
                        "Keep emergency contacts ready",
                        "Avoid distractions while walking",
                        "Use trusted transport services")));

        tipsList.add(new SafetyModel("Emergency Awareness",
                Arrays.asList("Keep your phone charged",
                        "Enable GPS always",
                        "Use SOS immediately",
                        "Memorize emergency numbers",
                        "Stay calm during danger")));

        tipsList.add(new SafetyModel("Online Safety",
                Arrays.asList("Never share OTPs",
                        "Avoid suspicious links",
                        "Use strong passwords",
                        "Enable two-factor authentication",
                        "Keep profiles private")));

        tipsList.add(new SafetyModel("Self Defense Tips",
                Arrays.asList("Learn basic self defense",
                        "Use loud voice for help",
                        "Stay mentally prepared",
                        "Target weak points if attacked",
                        "Carry legal safety tools")));

        tipsList.add(new SafetyModel("Travel Safety",
                Arrays.asList("Inform family before travel",
                        "Avoid solo late-night travel",
                        "Use verified taxi services",
                        "Keep ID proof available")));

        tipsList.add(new SafetyModel("Public Safety",
                Arrays.asList("Trust your instincts",
                        "Stay aware of surroundings",
                        "Avoid unsafe crowds",
                        "Locate emergency exits",
                        "Do not reveal personal information")));

        SafetyTipsAdapter adapter = new SafetyTipsAdapter(tipsList);
        recyclerView.setAdapter(adapter);
    }
}