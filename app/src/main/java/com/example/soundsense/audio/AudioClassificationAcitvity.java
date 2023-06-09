package com.example.soundsense.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.soundsense.helpers.AudioHelperActivity;

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
    //var per temporizzare l' invio delle email
    private long lastCallTime = 0;
    private static long MINUTES = 60 * 1000; // 5 minuti in millisecondi

    //TODO completare parte grafica per la selezione delle categorie
    private String objectOfAudio;
    private String objectOfAudioPrecedente = "";
    private String emailTo;
    private HashMap<String, Long> userClassification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        emailTo = sharedPreferences.getString("email", "");
        MINUTES *= Integer.parseInt(sharedPreferences.getString("timeout", "1"));

        userClassification = new HashMap<String, Long>();
        userClassification.put("Dog", 0L);
        userClassification.put("Speech", 0L);
        userClassification.put("Clapping", 0L);

        //inizialize audioClassifier from TF model
        try {
            audioClassifier = AudioClassifier.createFromFile(this, model);
        }catch (IOException e){
            e.printStackTrace();
        }

        //inizialize audio recorder for classifying audio
        tensorAudio = audioClassifier.createInputTensorAudio();
    }

    @Override
    public void startRecording(View view) {
        super.startRecording(view);
        TensorAudio.TensorAudioFormat format = audioClassifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n" +
                "Sample Rate: " + format.getSampleRate();
        tvSpecs.setText(specs);
        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                String categoryLabel;

                //filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output){
                    for (Category category : classifications.getCategories()){
                        //if score is higher than 30% possibility...
                        categoryLabel = category.getLabel();
                        //TODO potremmo usare un array creato dall' utente per le categorie
                        if(category.getScore() > 0.3f && userClassification.get(categoryLabel)!=null){
                            finalOutput.add(category);
                            objectOfAudio = categoryLabel;
                            sendEmail(emailTo,
                                    categoryLabel,
                                    "Yes, it's working well\nI will use it always.");
                        }
                    }
                }

                //Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for(Category category : finalOutput){
                    outputStr.append(category.getLabel())
                            .append(": ").append(getCurrentDateTime()).append("\n");
                    Log.i(TAG, outputStr.toString());
                }

                //updating the textView for output
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(finalOutput.isEmpty())
                            tvOutput.setText("Could not classify audio");
                        tvOutput.setText(outputStr.toString());
                    }
                });
            }
        };
        //after 1 second it start and every 0.5 second will classify audio
        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }

    @Override
    public void stopRecording(View view) {
        super.stopRecording(view);
        timerTask.cancel();
        audioRecord.stop();
    }

    public void sendEmail(String sendTo, String subject, String message){

        long currentTime = System.currentTimeMillis();
        if ((currentTime - userClassification.get(subject)) > MINUTES) {
            SendMail mail = new SendMail(
                    "gruppo.cinque.webd@gmail.com",
                    "daugjanscvsqdrab",
                    sendTo,
                    subject,
                    message
            );
            mail.execute();
            // Aggiorna il tempo dell'ultima chiamata
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.i("valore timestamp prima: ", userClassification.get(subject)+"");

                userClassification.replace(subject, currentTime);
            }


        } else {
            // La funzione non può essere chiamata perché sono passati meno di 5 minuti
            Log.i(TAG, "EMAIL TIMEOUT NON ANCORA TERMINATO");
            Log.i(TAG, (currentTime - userClassification.get(subject) > MINUTES) +"");

        }
    }

    public String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
