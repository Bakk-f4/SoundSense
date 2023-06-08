package com.example.soundsense.helpers;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundsense.R;

public class InputDataActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextName, editTextSurname;
    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_data_activity);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String surname = editTextSurname.getText().toString().trim();

                if (email.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                    Toast.makeText(InputDataActivity.this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show();
                } else {
                    // Fai qualcosa con i dati inseriti dall'utente
                    Toast.makeText(InputDataActivity.this, "Dati inviati: Email: " + email + ", Nome: " + name + ", Cognome: " + surname, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
