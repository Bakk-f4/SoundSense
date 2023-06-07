package com.example.soundsense.helpers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;

import com.example.soundsense.R;

public class AudioHelperActivity extends AppCompatActivity {

    protected TextView tvOutput;
    protected TextView tvSpecs;
    protected Button bttStartRecording;
    protected Button bttStopRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_helper);

        tvOutput = findViewById(R.id.tvAudioOutput);
        tvSpecs = findViewById(R.id.tvAudioSpecs);
        bttStartRecording = findViewById(R.id.bttStartRecording);
        bttStopRecording = findViewById(R.id.bttStopRecording);

        //disable stop recording at start of the activity
        bttStopRecording.setEnabled(false);

        //check if permission is granted
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
        }
    }

    public void startRecording(View view){
        bttStartRecording.setEnabled(false);
        bttStopRecording.setEnabled(true);
    }

    public void stopRecording(View view){
        bttStartRecording.setEnabled(true);
        bttStopRecording.setEnabled(false);
    }
}