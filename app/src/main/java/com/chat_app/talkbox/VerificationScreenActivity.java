package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationScreenActivity extends AppCompatActivity {

    EditText num1,num2,num3,num4,num5,num6;
    String getOtp, contactNumber;
    TextView resentOtp;
    int time = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_screen);

        getSupportActionBar().hide();

        getOtp = getIntent().getStringExtra("OTP");
        contactNumber = getIntent().getStringExtra("ContactNumber");

        final ProgressBar prgBarVerifyOtp = findViewById(R.id.prgBar_verify_otp);
        resentOtp = findViewById(R.id.resend_otp);

        ImageView back = findViewById(R.id.back_btn);
        final Button confirmCode = findViewById(R.id.confirm_code);
        num1 = findViewById(R.id.vr_1);
        num2 = findViewById(R.id.vr_2);
        num3 = findViewById(R.id.vr_3);
        num4 = findViewById(R.id.vr_4);
        num5 = findViewById(R.id.vr_5);
        num6 = findViewById(R.id.vr_6);

        displayTimer();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Phone_SignIn.class));
            }
        });

        confirmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!num1.getText().toString().trim().isEmpty() && !num2.getText().toString().trim().isEmpty() && !num3.getText().toString().trim().isEmpty()
                        && !num4.getText().toString().trim().isEmpty()&& !num5.getText().toString().trim().isEmpty()&& !num6.getText().toString().trim().isEmpty()){
                    String enteredOtp = num1.getText().toString() +
                            num2.getText().toString() +
                            num3.getText().toString() +
                            num4.getText().toString() +
                            num5.getText().toString() +
                            num6.getText().toString();

                    if(getOtp != null){
                        prgBarVerifyOtp.setVisibility(View.VISIBLE);
                        confirmCode.setVisibility(View.INVISIBLE);

                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                                getOtp, enteredOtp
                        );
                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        prgBarVerifyOtp.setVisibility(View.GONE);
                                        confirmCode.setVisibility(View.VISIBLE);
                                        if(task.isSuccessful()){
                                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(),"Enter Correct OTP!",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(getApplicationContext(),"Please check Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please enter all numbers",Toast.LENGTH_SHORT).show();
                }
            }
        });

        numberOtpMove();

        resentOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91" + getIntent().getStringExtra("ContactNumber"),
                        60,
                        TimeUnit.SECONDS,
                        VerificationScreenActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newOtp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(newOtp, forceResendingToken);
                                getOtp = newOtp;
                                displayTimer();
                            }
                        }
                );
            }
        });
    }

    public void displayTimer(){
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                resentOtp.setText("0:"+checkDigit(time));
                time--;
            }

            public void onFinish() {
                resentOtp.setText("RESEND OTP AGAIN");
            }

        }.start();
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private void numberOtpMove() {
        num1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    num2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        num2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    num3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        num3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    num4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        num4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    num5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        num5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    num6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}