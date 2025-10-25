package com.decryption.encryptionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.decryption.encryptionapp.databinding.ActivityDescryptBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptActivity extends AppCompatActivity {

    ActivityDescryptBinding binding;
    private static final int PERMISSION_REQUEST = 9999;
    private static final int REQUEST_CODE_PICK_FILE = 111;


    private Uri fileUri;
    private String selected_text;

    private  String AES_KEY = "0123456789abcdef";
    private  String AES_IV = "1234567890abcdef";

    private String re_decryption = "";

    private String finalInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDescryptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){


            if (higherPermissions()){
                Log.d("PERMISSIONS: ","Permissions enabled!");

            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST);
            }
        }else {
            if (checkStoragePermissions()) {
                Log.d("PERMISSIONS: ","Permissions enabled!");

            } else {

                requestStoragePermissions();
            }
        }


        binding.txtSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFilePicker();
            }
        });


        binding.btnDecryptFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                authenticateUser();


            }
        });

        binding.btnReDecryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initReDecryption();
            }
        });

        binding.btnSaveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (re_decryption.equalsIgnoreCase("")){
                    Toast.makeText(DecryptActivity.this, "First re decrypt file!", Toast.LENGTH_SHORT).show();
                }else {
                    String fileName = "decrypted_text"+System.currentTimeMillis()+".txt";
                    saveTextFileToDownloads(re_decryption,fileName);
                }

            }
        });





    }

    private void authenticateUser() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(DecryptActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();

                if (fileUri !=null){
//                    finalInput = decrypt(selected_text);
//                    binding.itemScroll.setVisibility(View.VISIBLE);
//                    binding.textDecryptAnswer.setText(finalInput);
//
//                    binding.btnDecryptFile.setEnabled(false);
//                    binding.btnSaveFile.setVisibility(View.VISIBLE);
                    initBlowFishDecryption();

                }else {
                    Toast.makeText(DecryptActivity.this, "Please select file!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(DecryptActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(DecryptActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Secure Vault Authentication")
                .setSubtitle("Use your fingerprint to access the vault")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }


    private void initBlowFishDecryption(){

        try {
            // Key for Blowfish
            String key = "reencrypt_key";  // Ensure the key is of appropriate length for Blowfish
            byte[] keyData = key.getBytes("UTF-8");
            SecretKeySpec ks = new SecretKeySpec(keyData, "Blowfish");

            // Encrypt data
            Cipher cipher = Cipher.getInstance("Blowfish");
            // Decrypt data
            byte[] decodedData = Base64.decode(selected_text, Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, ks);
            byte[] decryptedData = cipher.doFinal(decodedData);
            String decryptedText = new String(decryptedData, "UTF-8");
            finalInput = decryptedText;
            binding.itemScroll.setVisibility(View.VISIBLE);
            binding.textDecryptAnswer.setText(finalInput);

            binding.btnDecryptFile.setEnabled(false);
            binding.btnReDecryption.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void initReDecryption(){
        try {
            // Key for Blowfish
            String key = "mysecretkey";  // Ensure the key is of appropriate length for Blowfish
            byte[] keyData = key.getBytes("UTF-8");
            SecretKeySpec ks = new SecretKeySpec(keyData, "Blowfish");

            // Encrypt data
            Cipher cipher = Cipher.getInstance("Blowfish");
            // Decrypt data
            byte[] decodedData = Base64.decode(finalInput, Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, ks);
            byte[] decryptedData = cipher.doFinal(decodedData);
            String decryptedText = new String(decryptedData, "UTF-8");
            re_decryption = decryptedText;
//            binding.itemScroll.setVisibility(View.VISIBLE);
            binding.textDecryptAnswer.setText(re_decryption);
            binding.btnReDecryption.setEnabled(false);
            binding.btnSaveFile.setVisibility(View.VISIBLE);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain"); // Filter for plain text files
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private boolean checkStoragePermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean higherPermissions(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                fileUri = data.getData();
                String fileName = getFileName(fileUri);
                selected_text = getTextFromUri(fileUri);
                if (fileName != null) {
                    binding.txtFileName.setVisibility(View.VISIBLE);
                    binding.btnSaveFile.setVisibility(View.GONE);
                    binding.btnDecryptFile.setEnabled(true);
                    binding.btnDecryptFile.setVisibility(View.VISIBLE);
                    binding.txtFileName.setText("Selected File: " + fileName);
//                    Toast.makeText(this, selected_text, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error getting file name", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private String getFileName(Uri uri) {
        String fileName = null;
        try {
            ContentResolver contentResolver = getContentResolver();
            fileName = contentResolver.getType(uri);
            if (fileName != null && fileName.startsWith("text/plain")) {
                // Extract filename from the URI path
                fileName = new File(uri.getPath()).getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public String getTextFromUri(Uri fileUri) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public String decrypt(String encryptedText) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
            SecretKey key = new SecretKeySpec(AES_KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            AlgorithmParameterSpec spec = new IvParameterSpec(AES_IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decodedBytes = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveTextFileToDownloads(String text, String filename) {
        try {
            // Get the external Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Create the file
            File file = new File(downloadsDir, filename);

            // Write the text to the file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(text.getBytes());
            fos.close();

            // Show a toast message indicating successful saving
//            Log.d("SAVED","File saved");
            Toast.makeText(this, "Text file saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Show a toast message indicating error
            binding.txtFileName.setText("Error: "+e.getMessage());
            Toast.makeText(this, "Error saving text file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}