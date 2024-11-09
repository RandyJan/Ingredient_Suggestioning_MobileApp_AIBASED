package com.example.aibasedingredient;

import android.app.Dialog;
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

import com.example.aibasedingredient.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class Signin extends AppCompatActivity {

    ActivitySigninBinding signUpAct;
    EditText usernameField;
    EditText emailField;
    EditText passwordField;
    EditText confirmPasswordField;
    Context currentContext;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        signUpAct = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(signUpAct.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView loginText = signUpAct.loginText;
        loginText.setOnClickListener(c->{
            startActivity(new Intent(this, MainActivity.class));
        });

        usernameField = signUpAct.signUpUsernameField;
        emailField = signUpAct.signUpEmailField;
        passwordField = signUpAct.signUpPasswordField;
        confirmPasswordField = signUpAct.signUpConfirmPasswordField;

        Button signUpButton = signUpAct.signUpButton;
        signUpButton.setOnClickListener(c->SignUpButtonClicked());

        currentContext = this;

        auth = MainActivity.auth;

        FirebaseUser user = auth.getCurrentUser();

        if(user != null){
            startActivity(new Intent(this, homescreen.class));
        }
    }

    private void SignUpButtonClicked(){
        String username = String.valueOf(usernameField.getText());
        String email = String.valueOf(emailField.getText());
        String password = String.valueOf(passwordField.getText());
        String confirmPassword = String.valueOf(confirmPasswordField.getText());

        if(password.isEmpty()){
            Toast.makeText(currentContext, "Enter password first", Toast.LENGTH_LONG).show();
            return;
        }

        if(confirmPassword.isEmpty()){
            Toast.makeText(currentContext, "Confirm password first", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("Inputted username", username);
        Log.d("Inputted email", email);
        Log.d("password username", password);
        Log.d("Inputted confirmPassword", confirmPassword);

        if(!password.equals(confirmPassword)){
            Toast.makeText(currentContext, "Password doesn't match", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(currentContext, "Account created successfully", Toast.LENGTH_LONG).show();
                    FirebaseUser newlyRegisteredUser = MainActivity.auth.getCurrentUser();
                    String userID = newlyRegisteredUser.getUid();
                    UsersClass newUser = new UsersClass(userID, newlyRegisteredUser.getEmail(), username);
                    DatabaseReference reference = MainActivity.database.getReference();
                    reference.child("Users").child(userID).setValue(newUser);
                    startActivity(new Intent(currentContext, homescreen.class));
                }
                else{
                    Toast.makeText(currentContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });
    }
}