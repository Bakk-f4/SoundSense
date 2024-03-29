package com.example.soundsense.audio;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.soundsense.R;
import com.example.soundsense.helpers.AudioHelperActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import papaya.in.sendmail.SendMail;

public class AudioClassificationAcitvity extends AudioHelperActivity {

    private String model = "lite-model_yamnet_classification_tflite_1.tflite";
    private final String TAG = "AudioIdentificationActivity";
    private AudioRecord audioRecord;
    private TimerTask timerTask;
    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;
    private static long MINUTES = 60 * 1000; // minuti in millisecondi
    private String emailTo, userName, eventTime;
    private HashMap<String, Long> userClassification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create notification channel
        createNotificationChannel();

        //loading shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        emailTo = sharedPreferences.getString("email", "");
        userName = sharedPreferences.getString("name", "utente");

        MINUTES *= Integer.parseInt(sharedPreferences.getString("timeout", "1"));
        userClassification = new HashMap<String, Long>();

        //prendiamo dalla sharedPrefence le categorie dell' utente
        //get from shared preferences user categories
        String userCategoriesSharedPreference = sharedPreferences.getString("UserCategories", "");
        if(userCategoriesSharedPreference.equals("")){
            Toast.makeText(this, "You must select categories!", Toast.LENGTH_LONG).show();
        } else {
            try {
                JSONArray savedJsonArray = new JSONArray(userCategoriesSharedPreference);
                //per ogni elemento in savedJsonArrayd
                for (int i = 0; i < savedJsonArray.length(); i++) {
                    userClassification.put(savedJsonArray.getString(i), 0L);
                    Log.i("userClassification", savedJsonArray.getString(i));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            //inizialize audioClassifier from TF model
            try {
                audioClassifier = AudioClassifier.createFromFile(this, model);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //inizialize audio recorder for classifying audio
            tensorAudio = audioClassifier.createInputTensorAudio();
        }
    }

    @Override
    public void startRecording(View view) {
        super.startRecording(view);
        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                String categoryLabel = "";

                //filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        //if score is higher than 30% possibility...
                        categoryLabel = category.getLabel();
                        if (category.getScore() > 0.3f && userClassification.get(categoryLabel) != null) {
                            finalOutput.add(category);
                            eventTime = getCurrentDateTime();
                            //if waiting time is done
                            if (checkTime(categoryLabel)) {
                                if (sharedPreferences.getBoolean("checkEmail", false)) {
                                    sendEmail(categoryLabel);
                                }
                                if (sharedPreferences.getBoolean("checkNotification", false)) {
                                    myMessage("Abbiamo rilevato un evento audio: " + categoryLabel, category.getIndex());
                                }
                            }
                        }
                    }
                }

                //Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr.append(category.getLabel())
                            .append(": ").append(eventTime).append("\n");
                    Log.i(TAG, outputStr.toString());
                }

                //updating the textView for output
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalOutput.isEmpty())
                            tvOutput.setText("Could not classify audio");
                        tvOutput.setText(outputStr.toString());
                    }
                });
            }
        };
        //after 1 second it start and every 0.5 second will classify audio
        new Timer().scheduleAtFixedRate(timerTask, 0, 100);
    }

    @Override
    public void stopRecording(View view) {
        super.stopRecording(view);
        timerTask.cancel();
        audioRecord.stop();
    }

    public void sendEmail(String category) {

        String emailSubject = "Sound Sense:  " + category;

        String emailBody = "Ciao, " + userName +
                "\n\nAbbiamo rilevato un evento audio: " + category +
                " alle ore: " + eventTime;

        SendMail mail = new SendMail(
                "gruppo.cinque.webd@gmail.com",
                "daugjanscvsqdrab",
                emailTo,
                emailSubject,
                emailBody
        );
        mail.execute();
    }

    public String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ASNotificationN"; //getString(R.string.channel_name);
            String description = "ASNotificationD"; //getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("ASNotificationID", name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void myMessage(String message, Integer notificationId) {
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ASNotificationID")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("AudioSense")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    public Boolean checkTime(String category){
        long currentTime = System.currentTimeMillis();
        if ((currentTime - userClassification.get(category)) > MINUTES) {
            // Aggiorna il tempo dell'ultima chiamata
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userClassification.replace(category, currentTime);
            }
            return true;
        }
        // La funzione non può essere chiamata perché sono passati meno di 5 minuti
        Log.i(TAG, "EMAIL TIMEOUT NON ANCORA TERMINATO");
        Log.i(TAG, (currentTime - userClassification.get(category) > MINUTES) +"");
        return false;
    }
}
