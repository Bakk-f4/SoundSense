package com.example.soundsense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.soundsense.audio.AudioClassificationAcitvity;
import com.example.soundsense.helpers.AudioHelperActivity;
import com.example.soundsense.helpers.InputDataActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGoToAudioActivity(View view){
        // start the audio helper activity
        if(mailIsSet()){
            Intent intent = new Intent(this, AudioClassificationAcitvity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "You must set your email before use the app!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, InputDataActivity.class);
            startActivity(intent);
        }

    }

    private boolean mailIsSet(){
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        Log.i("mail is set", sharedPreferences.getString("email", ""));
        return !sharedPreferences.getString("email", "").equals("");

    }


}