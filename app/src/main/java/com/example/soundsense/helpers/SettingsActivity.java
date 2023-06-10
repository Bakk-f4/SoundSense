package com.example.soundsense.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsense.R;
import com.example.soundsense.audio.AudioClassificationAcitvity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextName, editTextSurname, editTextTimeout;
    private Button buttonSubmit;
    private String email;
    private String name;
    private String surname;
    private String timeout;
    private SharedPreferences sharedPreferences;
    private  ArrayAdapter<String> adapter;
    private static ArrayList<String> finalListCategory = new ArrayList<>();
    private ArrayList<String> yamnetClassList = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get all classification from yamnet.json file
        String[] yamnetClassArray = fromJSONToArray("yamnet_class_map.json");
        for (String yamnetClass : yamnetClassArray) {
            Log.i("YamnetClass", yamnetClass);
        }
        yamnetClassList.addAll(Arrays.asList(yamnetClassArray));
        Spinner spinner = findViewById(R.id.spinnerCategory);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yamnetClassArray);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id != 0) {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    Log.i("selectedItem", selectedItem);
                    yamnetClassList.remove(selectedItem);  // Rimuovi l'elemento dalla lista temporanea

                    ArrayAdapter<String> newAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, yamnetClassList);
                    spinner.setAdapter(newAdapter);  // Imposta il nuovo adapter

                    finalListCategory.add(selectedItem);  // Aggiungi l'elemento alla lista "finalListCategory"
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nessuna selezione effettuata
            }
        });


        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextTimeout = findViewById(R.id.editTextTimeOutEmail);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
        name = sharedPreferences.getString("name", "");
        surname = sharedPreferences.getString("surname", "");
        timeout = sharedPreferences.getString("timeout", "");

        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextSurname.setText(surname);
        editTextTimeout.setText(timeout);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = editTextEmail.getText().toString().trim();
                name = editTextName.getText().toString().trim();
                surname = editTextSurname.getText().toString().trim();
                timeout = editTextTimeout.getText().toString().trim();
                if(!isNumber(timeout)){
                    Toast.makeText(SettingsActivity.this, "Insert a valid number for Timeout", Toast.LENGTH_LONG).show();
                    timeout = "";
                }
                if(!validateEmail(email)){
                    Toast.makeText(SettingsActivity.this, "Insert a valid email for example your@email.com", Toast.LENGTH_LONG).show();
                    email = "";
                }
                if (email.isEmpty() || timeout.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
                } else {
                    //memorizzo le preferenze del client in modo permanente
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("surname", surname);
                    editor.putString("email", email);
                    editor.putString("timeout", timeout);
                    editor.apply();

                    Toast.makeText(SettingsActivity.this, "Ready to start", Toast.LENGTH_LONG).show();
                    onGoToAudioClassificationActivity(v);
                }
            }
        });
    }

    public void onGoToAudioClassificationActivity(View view){
        // start the audio helper activity
        Intent intent = new Intent(this, AudioClassificationAcitvity.class);
        startActivity(intent);
    }

    public static boolean validateEmail(String email) {
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isNumber(String input) {
        String pattern = "-?\\d+(\\.\\d+)?";
        return Pattern.matches(pattern, input);
    }

    public String[] fromJSONToArray(String fileName) {
        String[] ret = null;

        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonContent = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(jsonContent);

            ret = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String displayName = jsonObject.getString("display_name");
                ret[i] = displayName;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    //get the finalListCategory
    public ArrayList<String> getFinalListCategory() {
        return finalListCategory;
    }


}
