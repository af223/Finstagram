package com.codepath.finstagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * This activity allows a user to create an account by providing a (unique) username and password.
 *
 * This activity is started by LoginActivity.java if the user clicked the sign up button.
 */

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etUsername);
        btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                createUser(username, password);
            }
        });
    }

    private void createUser(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Intent i = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    Toast.makeText(SignupActivity.this, "Signup success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupActivity.this, "Unable to create account", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Unable to signup: ", e);
                }
            }
        });
    }
}