package com.example.bayuharisaputro.qrattendance_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.provider.Settings.Secure;
import android.widget.EditText;


public class RegistrasiActivity extends AppCompatActivity {
private String android_id ;
private EditText editNomor;
private EditText editNim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);
        editNim = findViewById(R.id.editTextNim);
        editNomor = findViewById(R.id.editTextPhone);
        android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);




    }
}
