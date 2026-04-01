package com.myandroid.nariguard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VoiceRecordActivity extends AppCompatActivity {

    private ImageButton btnRecord;
    private TextView tvTimer;
    private RecyclerView recyclerView;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private String outputFilePath;
    private boolean isRecording = false;

    private ArrayList<VoiceRecord> recordsList;
    private VoiceRecordAdapter adapter;

    private File recordsFolder;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;

    private static final int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);

        btnRecord = findViewById(R.id.btnRecord);
        tvTimer = findViewById(R.id.tvTimer);
        recyclerView = findViewById(R.id.rvVoiceRecords);

        recordsList = new ArrayList<>();
        adapter = new VoiceRecordAdapter(recordsList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Folder for recordings
        recordsFolder = new File(
                getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "VoiceRecords"
        );

        if (!recordsFolder.exists()) {
            recordsFolder.mkdirs();
        }

        loadExistingRecords();
        checkPermissions();

        btnRecord.setOnClickListener(v -> {
            if (!hasMicrophonePermission()) {
                checkPermissions();
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isRecording) startRecording();
            else stopRecording();
        });
    }

    // ---------------- PERMISSIONS ----------------

    private boolean hasMicrophonePermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- RECORDING ----------------

    private void startRecording() {
        try {
            // Ensure folder exists
            if (!recordsFolder.exists()) recordsFolder.mkdirs();

            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(new Date());

            File file = new File(recordsFolder, "record_" + timeStamp + ".3gp");
            outputFilePath = file.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(outputFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            btnRecord.setImageResource(R.drawable.stop);
            startTimer();

            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder == null) return;

        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            // Delete corrupted file
            new File(outputFilePath).delete();
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        isRecording = false;
        stopTimer();
        tvTimer.setText("00:00:00");
        btnRecord.setImageResource(R.drawable.mic);

        File file = new File(outputFilePath);
        if (file.exists()) {
            recordsList.add(0, new VoiceRecord(
                    file.getName(),
                    file.getAbsolutePath(),
                    getLastModifiedTime(file)
            ));
            adapter.notifyItemInserted(0);
        }

        Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
    }

    // ---------------- TIMER ----------------

    private void startTimer() {
        seconds = 0;

        timerHandler.post(new Runnable() {
            @Override
            public void run() {
                int hrs = seconds / 3600;
                int min = (seconds % 3600) / 60;
                int sec = seconds % 60;

                tvTimer.setText(String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hrs, min, sec
                ));

                seconds++;
                timerHandler.postDelayed(this, 1000);
            }
        });
    }

    private void stopTimer() {
        timerHandler.removeCallbacksAndMessages(null);
    }

    // ---------------- LOAD RECORDS ----------------

    private void loadExistingRecords() {
        recordsList.clear();
        File[] files = recordsFolder.listFiles();

        if (files != null) {
            for (File f : files) {
                recordsList.add(new VoiceRecord(
                        f.getName(),
                        f.getAbsolutePath(),
                        getLastModifiedTime(f)
                ));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private String getLastModifiedTime(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault()
        );
        return sdf.format(new Date(file.lastModified()));
    }

    // ---------------- PLAYBACK ----------------

    public void playRecording(String path) {
        try {
            if (mediaPlayer != null) mediaPlayer.release();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot play recording", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- LIFECYCLE ----------------

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        if (mediaRecorder != null) mediaRecorder.release();
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }
}