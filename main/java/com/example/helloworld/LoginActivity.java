package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    DbAdapter dbAdapter;  // Db adapter for the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Add this last after showing the username and password persist
        // Clear the username and password
        EditText etUsername = (EditText) findViewById(R.id.etUsername);
        EditText etPassword = (EditText) findViewById(R.id.etPassword);
        etUsername.setText("");
        etPassword.setText("");

        // Create an instance of the Db adapter
        dbAdapter = new DbAdapter(this);
    }

    // Sign in button
    public void btnSignInClick(View view) {
        // Get the username to pass
        EditText etUsername = (EditText) findViewById(R.id.etUsername);
        EditText etPassword = (EditText) findViewById(R.id.etPassword);
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        Log.d("LoginActivity", "btnSignInClick: Username = " + username + ", Password = " + password);

        // TODO: If this is a new username, add the user and password to a database
        // TODO: If this is an existing user, verify password to login
        // TODO: If login attempt is invalid, show an error modal
        String storedPassword = dbAdapter.userExistsAWS(username);
        if (storedPassword != null) {
            // The user exists, make sure the entered password is correct
            if (password.equals(storedPassword)) {
                // The password is correct, move to the temperature control screen
                Message.message(this,"Login Successful");

                // Go to the TemperatureControl Activity
                // https://developer.android.com/training/basics/firstapp/starting-activity
                Intent intent = new Intent(this, TemperatureControlActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                // Let the user know the pass was incorrect
                Message.message(this,"Password Invalid");

                // Clear the password edit text
                etPassword.setText("");
            }
        } else {
            // The user does not yet exist so add them to the database with the password entered
            dbAdapter.usersInsertAWS(username, password);
            Message.message(this,"User Added");
        }
    }
}