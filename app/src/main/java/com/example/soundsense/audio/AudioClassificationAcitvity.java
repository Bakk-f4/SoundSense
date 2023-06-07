package com.example.soundsense.audio;

import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.soundsense.helpers.AudioHelperActivity;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                //filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output){
                    for (Category category : classifications.getCategories()){
                        //if score is higher than 30% possibility...
                        if(category.getScore() > 0.3f && category.getLabel().equals("Dog")){
                            finalOutput.add(category);
                            sendEmail("ion.menghini@gmail.com",
                                    "Testing Email Sending",
                                    "Yes, it's working well\nI will use it always.");
                            // TODO AGGIUNGERE TIMEOUT DI INVIO
                            // TODO AGGIUNGERE PAGINA SETTING DA COMPILARE OBBLIGATORIAMENTE
                            //  ( tasto Start nn funziona (se disabilitato Toast di avviso ) )
                            //  si vedra'
                        }
                        if(category.getScore() > 0.3f && category.getLabel().equals("Speech")){
                            finalOutput.add(category);
                        }
                        if(category.getScore() > 0.3f && category.getLabel().equals("Bark")){
                            finalOutput.add(category);
                        }
                    }
                }

                //Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for(Category category : finalOutput){
                    outputStr.append(category.getLabel())
                            .append(": ").append(category.getScore()).append("\n");
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
        SendMail mail = new SendMail(
                "gruppo.cinque.webd@gmail.com",
                "daugjanscvsqdrab",
                sendTo,
                subject,
                message
        );
        mail.execute();
    }

}
