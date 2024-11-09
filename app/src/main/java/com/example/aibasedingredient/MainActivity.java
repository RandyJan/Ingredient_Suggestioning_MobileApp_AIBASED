package com.example.aibasedingredient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aibasedingredient.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
    Context currentContext;

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

        currentContext = this;

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
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(currentContext, "Login successful!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(currentContext, homescreen.class));
                }
                else{
                    Toast.makeText(currentContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}