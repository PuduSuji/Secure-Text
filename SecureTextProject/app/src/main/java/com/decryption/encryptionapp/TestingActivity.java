package com.decryption.encryptionapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.decryption.encryptionapp.databinding.ActivityTestingBinding;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ElGamalPrivateKeySpec;
import org.bouncycastle.jce.spec.ElGamalPublicKeySpec;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class TestingActivity extends AppCompatActivity {

    ActivityTestingBinding binding;
    Cipher cipher;
    SecretKeySpec ks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTestingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        String text = binding.demoText.getText().toString();

//        binding.btnEncrypt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    // Generate a new key pair
//                    KeyPair keyPair = ElGamalUtils.generateKeyPair();
//                    ElGamalPublicKeySpec publicKeySpec = (ElGamalPublicKeySpec) keyPair.getPublic();
//                    ElGamalPrivateKeySpec privateKeySpec = (ElGamalPrivateKeySpec) keyPair.getPrivate();
//
//                    // Encrypt data
//                    String originalText = "Hello, World!";
//                    String encryptedText = ElGamalUtils.encrypt(text, publicKeySpec);
////                    Log.d(TAG, "Encrypted: " + encryptedText);
//                    binding.demoText.setText(encryptedText);
//
//                    // Decrypt data
////                    String decryptedText = ElGamalUtils.decrypt(encryptedText, privateKeySpec);
////                    Log.d(TAG, "Decrypted: " + decryptedText);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(TestingActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        try {
            // Key for Blowfish
            String key = "mysecretkey";  // Ensure the key is of appropriate length for Blowfish
            byte[] keyData = key.getBytes();
            ks = new SecretKeySpec(keyData, "Blowfish");

            // Encrypt data
            String originalText = text;
            cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, ks);

            byte[] encryptedData = cipher.doFinal(originalText.getBytes("UTF-8"));
            String encryptedText = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encryptedText = Base64.getEncoder().encodeToString(encryptedData);
            }
//            Log.d(TAG, "Encrypted: " + encryptedText);
            binding.demoText.setText(encryptedText);


            // Decrypt data


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        binding.btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dec = binding.demoText.getText().toString();

                try {
                    cipher.init(Cipher.DECRYPT_MODE, ks);

                    byte[] decryptedData = new byte[0];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        decryptedData = cipher.doFinal(Base64.getDecoder().decode(dec));
                    }
                    String decryptedText = new String(decryptedData);
                    binding.demoText.setText(decryptedText);
                }catch (Exception e){
                    e.getMessage();
                }
//                    Log.d(TAG, "Decrypted: " + decryptedText);
            }
        });



    }
}