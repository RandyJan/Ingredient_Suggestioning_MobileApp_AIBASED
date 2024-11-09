package com.example.aibasedingredient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aibasedingredient.databinding.ActivityHomescreenBinding;

public class homescreen extends AppCompatActivity {

    ActivityHomescreenBinding homeScreemAct;
    Context currentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        homeScreemAct = ActivityHomescreenBinding.inflate(getLayoutInflater());
        setContentView(homeScreemAct.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentContext = this;

        ImageButton logoutButton = homeScreemAct.logoutButton;
        logoutButton.setOnClickListener(c->LogoutUser());
    }

    private void LogoutUser(){
        MainActivity.auth.signOut();
        startActivity(new Intent(currentContext, MainActivity.class));
    }
}