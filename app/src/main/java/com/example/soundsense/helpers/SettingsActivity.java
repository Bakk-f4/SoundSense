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
import android.widget.Switch;
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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextName, editTextSurname, editTextTimeout;
    private static Switch switchSendEmail;
    private static Switch switchSendNotification;
    private Button buttonSubmit, buttonReset;
    private String email, name, surname, timeout;
    private SharedPreferences sharedPreferences;
    private  ArrayAdapter<String> adapter;
    private static ArrayList<String> finalListCategory = new ArrayList<>();
    private ArrayList<String> yamnetClassList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //load data from sharedPreference
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //initialize UI objects
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextTimeout = findViewById(R.id.editTextTimeOutEmail);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonReset = findViewById(R.id.bttReset);
        switchSendEmail = findViewById(R.id.switchEmail);
        switchSendNotification = findViewById(R.id.switchNotification);

        String userCategories = sharedPreferences.getString("UserCategories", "");
        if(userCategories.equals(""))
            buttonReset.setEnabled(false);


        //check if we didnt loaded already yamnet categories
        String loadedYamnetFromSharedPref = sharedPreferences.getString("allYamnetCategories", "");
        if(loadedYamnetFromSharedPref.equals("")){

            //get all classification from yamnet.json file
            String[] yamnetClassArray = fromJSONToArray("short_list_category.json");
            yamnetClassList.addAll(Arrays.asList(yamnetClassArray));
            Collections.sort(yamnetClassList);

            //preparing data for sharedPreference
            JSONArray jsonArray = new JSONArray(yamnetClassList);
            String arrayString = jsonArray.toString();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("allYamnetCategories", arrayString);
            editor.apply();
        } else {
            try {
                // Convert string from array into JSONArray
                JSONArray savedJsonArray = new JSONArray(loadedYamnetFromSharedPref);
                for (int i = 0; i < savedJsonArray.length(); i++) {
                    yamnetClassList.add(savedJsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //load the data from yamnetClassList into the Spinner
        Spinner spinner = findViewById(R.id.spinnerCategory);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yamnetClassList);
        spinner.setAdapter(adapter);

        //on user selection item from spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id != 0) {
                    //remove the element from the temp list
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    yamnetClassList.remove(selectedItem);

                    //preparing data for sharedPreference
                    JSONArray jsonArray = new JSONArray(yamnetClassList);
                    String arrayString = jsonArray.toString();

                    //removing the element from the sharedPreference allYamnetCategories
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("allYamnetCategories", arrayString);
                    editor.apply();

                    // set new adapter
                    ArrayAdapter<String> newAdapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, yamnetClassList);
                    spinner.setAdapter(newAdapter);

                    // add element to list "finalListCategory"
                    finalListCategory.add(selectedItem);

                    //insert finalListCategory to sharedPreference
                    jsonArray = new JSONArray(finalListCategory);
                    arrayString = jsonArray.toString();
                    editor = sharedPreferences.edit();
                    editor.putString("UserCategories", arrayString);
                    editor.apply();

                    //enabling reset button for categories
                    buttonReset.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing selected...
            }
        });

        //update the UI objects
        email = sharedPreferences.getString("email", "");
        name = sharedPreferences.getString("name", "");
        surname = sharedPreferences.getString("surname", "");
        timeout = sharedPreferences.getString("timeout", "");
        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextSurname.setText(surname);
        editTextTimeout.setText(timeout);
        switchSendEmail.setChecked(sharedPreferences.getBoolean("checkEmail", false));
        switchSendNotification.setChecked(sharedPreferences.getBoolean("checkNotification", false));

        //when user send the input data with buttonSubmit
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
                    //save the preference of user in the shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("surname", surname);
                    editor.putString("email", email);
                    editor.putString("timeout", timeout);
                    editor.putBoolean("checkEmail", switchSendEmail.isChecked());
                    editor.putBoolean("checkNotification", switchSendNotification.isChecked());
                    editor.apply();

                    onGoToAudioClassificationActivity(v);
                }
            }
        });

        //when user use reset button
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //grab UserCategories from sharedPref
                String userCategories = sharedPreferences.getString("UserCategories", "");
                JSONArray savedJsonArray = null;
                try {
                    savedJsonArray = new JSONArray(userCategories);
                    Log.i("userCategories", savedJsonArray.toString());
                    //for each userCategory, insert it into the yamnetClassList
                    for (int i = 0; i < savedJsonArray.length(); i++) {
                        yamnetClassList.add(savedJsonArray.getString(i));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                //sorting output
                Collections.sort(yamnetClassList);

                //cleaning both arrays
                finalListCategory.clear();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserCategories", "");
                editor.apply();

                //disable the button reset
                buttonReset.setEnabled(false);
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
}
