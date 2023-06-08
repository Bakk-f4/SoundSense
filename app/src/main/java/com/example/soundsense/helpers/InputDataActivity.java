package com.example.soundsense.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsense.R;
import com.example.soundsense.audio.AudioClassificationAcitvity;

public class InputDataActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextName, editTextSurname, editTextTimeout;
    private Button buttonSubmit;
    private String email;
    private String name;
    private String surname;
    private String timeout;
    private SharedPreferences sharedPreferences;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);
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

                if (email.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                    Toast.makeText(InputDataActivity.this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
                } else {

                    //memorizzo le preferenze del client in modo permanente
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.putString("surname", surname);
                    editor.putString("email", email);
                    editor.putString("timeout", timeout);
                    editor.apply();

                    Toast.makeText(InputDataActivity.this, "Dati inviati: Email: " + email + ", Nome: " + name + ", Cognome: " + surname, Toast.LENGTH_LONG).show();
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

}
