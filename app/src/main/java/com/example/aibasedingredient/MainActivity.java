package com.example.aibasedingredient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aibasedingredient.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainAct;
    EditText usernameField;
    EditText passwordField;
    public static FirebaseUser currentUser;
    public static FirebaseAuth auth;

    public static FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mainAct = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainAct.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView signUpText = mainAct.signUpText;
        signUpText.setOnClickListener(c->{
            startActivity(new Intent(this, Signin.class));
        });

        usernameField = mainAct.loginUsernameField;
        passwordField = mainAct.loginPasswordField;
        Button loginButton = mainAct.loginButton;
        loginButton.setOnClickListener(c->loginButtonClicked());
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        if(currentUser != null){
            startActivity(new Intent(this, homescreen.class));
        }
    }

    private void loginButtonClicked(){
        String username = String.valueOf(usernameField.getText());
        String password = String.valueOf(passwordField.getText());
        Log.d("Inputted username", username);
        Log.d("Inputted password", password);
    }
}