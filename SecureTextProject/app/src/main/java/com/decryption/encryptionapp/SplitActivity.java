package com.decryption.encryptionapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.decryption.encryptionapp.databinding.ActivitySplitBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class SplitActivity extends AppCompatActivity {


    ActivitySplitBinding binding;
    private static final int PERMISSION_REQUEST = 100;


    private static final int REQUEST_CODE_PICK_FILE = 101;

    private Uri fileUri;
    private String selected_text;
    private ArrayList<String> splitFiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplitBinding.inflate(getLayoutInflater());
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



        binding.btnSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lineCount = binding.inputLines.getText().toString();
                if (fileUri == null){
                    Toast.makeText(SplitActivity.this, "Please select file", Toast.LENGTH_SHORT).show();
                }else if (Integer.parseInt(lineCount) == 0 || Integer.parseInt(lineCount) < 0){
                    Toast.makeText(SplitActivity.this, "Enter valid line values", Toast.LENGTH_SHORT).show();

                }else {
                    splitFile();
                    binding.btnSplit.setVisibility(View.GONE);
                    binding.btnSave.setVisibility(View.VISIBLE);
                }

            }
        });



        binding.txtSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFilePicker();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataSaved();
            }
        });

    }
    private void splitFile() {
        splitFiles = new ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            int lineCount = 0;
            int maxLinesPerFile = Integer.parseInt(binding.inputLines.getText().toString()); // Change this value to control how many lines per split file

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
                lineCount++;
                if (lineCount >= maxLinesPerFile) {
                    splitFiles.add(builder.toString());
                    builder.setLength(0);
                    lineCount = 0;
                }
            }

            if (builder.length() > 0) {
                splitFiles.add(builder.toString());
            }

            reader.close();
            inputStream.close();

            binding.txtFileName.setText("File split into " + splitFiles.size() + " parts");
            Toast.makeText(this, "File split into " + splitFiles.size() + " parts", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("SplitFile", "Error splitting file", e);
            Toast.makeText(this, "Error splitting file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void dataSaved() {
        for (int i = 0; i < splitFiles.size(); i++) {
            String fileName = getFileName(fileUri)+"/"+"split_file_" + (i + 1) + ".txt";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SplitFiles");

            Uri fileUri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

            try (OutputStream os = getContentResolver().openOutputStream(fileUri)) {
                os.write(splitFiles.get(i).getBytes());
            } catch (IOException e) {
                Log.e("SaveFile", "Error saving file", e);
                Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Files saved successfully", Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied, inform user
                Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
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
                    binding.inputLines.setVisibility(View.VISIBLE);
                    binding.btnSplit.setVisibility(View.VISIBLE);
                    binding.btnSave.setVisibility(View.GONE);


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
}