package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Phone_SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone__sign_in);

        getSupportActionBar().hide();

        EditText contact = findViewById(R.id.et_contact);
        TextView number = findViewById(R.id.txt3);
        ImageView back = findViewById(R.id.back_btn);
        Button sendVerificationCode = findViewById(R.id.send_verification_code);

        ProgressBar prgBar = findViewById(R.id.prgBar);

//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), SignInPage.class));
//            }
//        });

        contact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number.setText("+91" + charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!contact.getText().toString().trim().isEmpty()) {
                    if (contact.length() == 10) {

                        prgBar.setVisibility(View.VISIBLE);
                        sendVerificationCode.setVisibility(View.INVISIBLE);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+91" + contact.getText().toString(),
                                60,
                                TimeUnit.SECONDS,
                                Phone_SignIn.this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        prgBar.setVisibility(View.GONE);
                                        sendVerificationCode.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        prgBar.setVisibility(View.GONE);
                                        sendVerificationCode.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String otp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        super.onCodeSent(otp, forceResendingToken);
                                        Intent intent = new Intent(Phone_SignIn.this, VerificationScreenActivity.class);
                                        intent.putExtra("ContactNumber", contact.getText().toString());
                                        intent.putExtra("OTP", otp);
                                        startActivity(intent);
                                    }
                                }
                        );
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter valid numbers!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Enter mobile numbers!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}