package com.decryption.encryptionapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SaveEncryptionTask extends AsyncTask<Void, Void, String> {
    private static final String SERVER_URL = "https://sobering-hills.000webhostapp.com/API/save_encryption.php";
    private static final String CHARSET = "UTF-8";

    private Context context;
    private String aesKey;
    private String encryptedText;
    private ProgressDialog progressDialog;

    public SaveEncryptionTask(Context context, String aesKey, String encryptedText) {
        this.context = context;
        this.aesKey = aesKey;
        this.encryptedText = encryptedText;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, "Saving Encryption", "Please wait...", true, false);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // Build the POST data
            String postData = "aes_key=" + URLEncoder.encode(aesKey, CHARSET) +
                    "&encrypted_text=" + URLEncoder.encode(encryptedText, CHARSET);

            // Create HTTP connection
            URL url = new URL(SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", CHARSET);
            connection.setUseCaches(false);

            // Write POST data to the connection
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(postData.getBytes(CHARSET));
            outputStream.flush();
            outputStream.close();

            // Get the response from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();
        if (result != null) {
            Toast.makeText(context, "Encryption saved successfully: "+result, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to save encryption"+result, Toast.LENGTH_SHORT).show();
        }
    }
}
