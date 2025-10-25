package com.decryption.encryptionapp;

import static com.decryption.encryptionapp.SecretKeyGenerator.generateSecretKey;

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

import com.decryption.encryptionapp.databinding.ActivityEncryptBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptActivity extends AppCompatActivity {


    ActivityEncryptBinding binding;
    private static final int PERMISSION_REQUEST = 100;


    private static final int REQUEST_CODE_PICK_FILE = 101;

    private Uri fileUri;

    private   String AES_KEY = "0123456789abcdef";
    private   String AES_IV = "1234567890abcdef";

    private String selected_text = "null";
    private String finalOutput ="";
    private String re_encryption = "";
    private ArrayList<String> splitFiles;

    private Cipher cipher;
    private SecretKeySpec ks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEncryptBinding.inflate(getLayoutInflater());
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


        binding.btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fileUri !=null){
//                    String secret_key = "hello123";
//                    finalOutput = encrypt(selected_text);

//                    try {
//                        cipher.init(Cipher.DECRYPT_MODE, ks);
//
//                        byte[] decryptedData = new byte[0];
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                            decryptedData = cipher.doFinal(java.util.Base64.getDecoder().decode(selected_text));
//                        }
//
//                    }catch (Exception e){
//                        e.getMessage();
//                    }
                    encryptMethod();

                    binding.itemScroll.setVisibility(View.VISIBLE);
                    binding.btnEncrypt.setEnabled(false);
//                    binding.textEncryptedAnswer.setText(finalOutput);

                    binding.btnReEncrypt.setVisibility(View.VISIBLE);

                }
            }
        });


        binding.btnReEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalOutput.isEmpty()){
                    Toast.makeText(EncryptActivity.this, "First encrypt data", Toast.LENGTH_SHORT).show();
                }else {


                    reencryptMethod();
                    binding.btnReEncrypt.setEnabled(false);
                    binding.btnGenerateKey.setVisibility(View.VISIBLE);

                }
            }
        });


        binding.btnGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AES_KEY = generateSecretKey(256);
                binding.btnEncrypt.setEnabled(false);
                binding.btnGenerateKey.setEnabled(false);
                binding.btnSaveFile.setEnabled(true);
                binding.btnReEncrypt.setEnabled(false);
                binding.btnSaveFile.setVisibility(View.VISIBLE);
                Toast.makeText(EncryptActivity.this, "Key generated successfully!", Toast.LENGTH_SHORT).show();
            }
        });


        binding.btnSaveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save file in files
                String fileName = "re_encrypted_text"+System.currentTimeMillis()+".txt";
                saveTextFileToDownloads(re_encryption,fileName);
                SaveEncryptionTask saveEncryptionTask = new SaveEncryptionTask(EncryptActivity.this, AES_KEY, finalOutput);
                saveEncryptionTask.execute();

//                dataSaved();
            }
        });


//        initBlowFish();



//        binding.btnSplit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (fileUri == null){
//                    Toast.makeText(EncryptActivity.this, "Please select file", Toast.LENGTH_SHORT).show();
//                }else {
//                    splitFile();
//
//                    binding.btnSaveFile.setVisibility(View.VISIBLE);
//                }
//
//            }
//        });

    }



//    private void splitFile() {
//        splitFiles = new ArrayList<>();
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(fileUri);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder builder = new StringBuilder();
//            String line;
//            int lineCount = 0;
//            int maxLinesPerFile = 2; // Change this value to control how many lines per split file
//
//            while ((line = reader.readLine()) != null) {
//                builder.append(line).append("\n");
//                lineCount++;
//                if (lineCount >= maxLinesPerFile) {
//                    splitFiles.add(builder.toString());
//                    builder.setLength(0);
//                    lineCount = 0;
//                }
//            }
//
//            if (builder.length() > 0) {
//                splitFiles.add(builder.toString());
//            }
//
//            reader.close();
//            inputStream.close();
//
//            binding.txtFileName.setText("File split into " + splitFiles.size() + " parts");
//            Toast.makeText(this, "File split into " + splitFiles.size() + " parts", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Log.e("SplitFile", "Error splitting file", e);
//            Toast.makeText(this, "Error splitting file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void dataSaved() {
//        for (int i = 0; i < splitFiles.size(); i++) {
//            String fileName = "split_file_" + (i + 1) + ".txt";
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SplitFiles");
//
//            Uri fileUri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);
//
//            try (OutputStream os = getContentResolver().openOutputStream(fileUri)) {
//                os.write(splitFiles.get(i).getBytes());
//            } catch (IOException e) {
//                Log.e("SaveFile", "Error saving file", e);
//                Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        Toast.makeText(this, "Files saved successfully", Toast.LENGTH_SHORT).show();
//    }

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
                    binding.btnEncrypt.setVisibility(View.VISIBLE);
                    binding.btnEncrypt.setEnabled(true);
                    binding.itemScroll.setVisibility(View.GONE);
                    binding.btnSaveFile.setVisibility(View.GONE);
                    binding.btnGenerateKey.setVisibility(View.GONE);
                    binding.btnReEncrypt.setVisibility(View.GONE);

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

    public  String encrypt(String input) {

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
            SecretKey key = new SecretKeySpec(AES_KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            AlgorithmParameterSpec spec = new IvParameterSpec(AES_IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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


//    private String encryptText(Uri fileUri, String secretKey) {
//        String encryptedText = "";
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(fileUri);
//            byte[] fileBytes = readBytesFromInputStream(inputStream); // Read file bytes
//
//            // Generate random initialization vector (IV)
//            SecureRandom random = new SecureRandom();
//            byte[] iv = new byte[16];
//            random.nextBytes(iv);
//
//            // Create AES Cipher instance in encryption mode
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
//
//            // Encrypt file bytes
//            byte[] encryptedBytes = cipher.doFinal(fileBytes);
//
//            // Concatenate IV and encrypted bytes
//            encryptedText = Base64.encodeToString(iv, Base64.DEFAULT) + ":" + Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return encryptedText;
//    }
//
//    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int nRead;
//        byte[] data = new byte[16384]; // You can adjust the buffer size according to your needs
//        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
//        buffer.flush();
//        return buffer.toByteArray();
//    }


    private void encryptMethod(){

        try {
            // Key for Blowfish
            String key = "mysecretkey";  // Ensure the key is of appropriate length for Blowfish
            byte[] keyData = key.getBytes("UTF-8");
            SecretKeySpec ks = new SecretKeySpec(keyData, "Blowfish");

            // Encrypt data
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, ks);
            byte[] encryptedData = cipher.doFinal(selected_text.getBytes("UTF-8"));
            String encryptedText = Base64.encodeToString(encryptedData, Base64.DEFAULT);
            binding.textEncryptedAnswer.setText(encryptedText);
            finalOutput = new String(encryptedText);
            binding.textEncryptedAnswer.setText(finalOutput);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }












    }

    private void reencryptMethod(){
        try {
            // Key for Blowfish
            String key = "reencrypt_key";  // Ensure the key is of appropriate length for Blowfish
            byte[] keyData = key.getBytes("UTF-8");
            SecretKeySpec ks = new SecretKeySpec(keyData, "Blowfish");

            // Encrypt data
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, ks);
            byte[] encryptedData = cipher.doFinal(finalOutput.getBytes("UTF-8"));
            String encryptedText = Base64.encodeToString(encryptedData, Base64.DEFAULT);
            re_encryption = new String(encryptedText);
            binding.textEncryptedAnswer.setText(re_encryption);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


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