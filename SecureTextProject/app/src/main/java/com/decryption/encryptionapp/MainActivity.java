package com.decryption.encryptionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.decryption.encryptionapp.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        binding.btnEncrypt.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, EncryptActivity.class));

        });

        binding.btnDecrypt.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this,DecryptActivity.class)));

        binding.btnSplit.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this,SplitActivity
                        .class)));



    }




}