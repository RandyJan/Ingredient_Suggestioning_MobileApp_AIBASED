package com.example.aibasedingredient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aibasedingredient.databinding.ActivityHomescreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class homescreen extends AppCompatActivity {

    ActivityHomescreenBinding homeScreemAct;
    Context currentContext;
    LinearLayout drawerView;
    Dialog popUpDialog;
    EditText allergyOrDislikeField;

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

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SetDrawerGone();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        drawerView = homeScreemAct.homescreenDrawer;
        ImageButton burgerButton = homeScreemAct.burgerButton;
        burgerButton.setOnClickListener(c->SetDrawerVisible());
        ImageButton addDislikeButton = homeScreemAct.addDislikesButton;
        addDislikeButton.setOnClickListener(c->showDialogForAddDislike());
        ImageButton addAllergyButton = homeScreemAct.addAllergicToButton;
        addAllergyButton.setOnClickListener(c->showDialogForAddAllergy());
        ImageButton logoutButton = homeScreemAct.logoutButton;
        logoutButton.setOnClickListener(c->LogoutUser());

        popUpDialog = new Dialog(this);
        popUpDialog.setContentView(R.layout.addingredient_popup);
        allergyOrDislikeField = popUpDialog.findViewById(R.id.allergyOrDislikeField);
        ImageButton closeDialogButton = popUpDialog.findViewById(R.id.closeAddingDialog);
        closeDialogButton.setOnClickListener(c->popUpDialog.dismiss());
    }



    private void LogoutUser(){
        MainActivity.auth.signOut();
        startActivity(new Intent(currentContext, MainActivity.class));
    }
    private void SetDrawerVisible(){
        drawerView.setVisibility(View.VISIBLE);
    }

    private void SetDrawerGone(){
        if(drawerView.getVisibility() == View.VISIBLE){
            drawerView.setVisibility(View.GONE);
        }
    }

    private void showDialogForAddDislike(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        confirmAdding.setOnClickListener(c->AddDislike());
        popUpDialog.show();
    }

    private void showDialogForAddAllergy(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        confirmAdding.setOnClickListener(c->AddAllergy());
        popUpDialog.show();
    }
    private void AddDislike(){
        String newDislike = String.valueOf(allergyOrDislikeField.getText());
        Log.d("Added item", newDislike);
        MainActivity.databaseReference.child("Dislikes").child(MainActivity.loggedInUser.userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String data = "";
                    if(task.getResult().child("data").exists()){
                        String existingDislike = task.getResult().child("data").getValue().toString();
                        List<String> dislikeList = new ArrayList<>();
                              dislikeList = Arrays.stream(existingDislike.split(",")).toList();
                              for(String item : dislikeList){
                                if(newDislike.toLowerCase().trim().equals(item.toLowerCase().trim())){
                                    Toast.makeText(currentContext, "Dislike is already listed", Toast.LENGTH_LONG).show();
                                    return;
                                }
                              }
                        data = existingDislike;
                    }
                    data += newDislike + ",";
                    DislikeOrAllergyClass dislikes = new DislikeOrAllergyClass(data);
                    MainActivity.databaseReference.child("Dislikes").child(MainActivity.loggedInUser.userID).setValue(dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(currentContext, "Adding dislike successful!", Toast.LENGTH_LONG).show();
                                allergyOrDislikeField.setText("");
                            }
                            else{
                                Toast.makeText(currentContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void AddAllergy(){
        String newAllergy = String.valueOf(allergyOrDislikeField.getText());
        Log.d("Added item", newAllergy);

        MainActivity.databaseReference.child("Allergies").child(MainActivity.loggedInUser.userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String data = "";
                    if(task.getResult().child("data").exists()){
                        String existingAllergy = task.getResult().child("data").getValue().toString();
                        List<String> allergyList = new ArrayList<>();
                        allergyList = Arrays.stream(existingAllergy.split(",")).toList();
                        for(String item : allergyList){
                            if(newAllergy.toLowerCase().trim().equals(item.toLowerCase().trim())){
                                Toast.makeText(currentContext, "Allergy is already listed", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        data = existingAllergy;
                    }
                    data += newAllergy + ",";
                    DislikeOrAllergyClass dislikes = new DislikeOrAllergyClass(data);
                    MainActivity.databaseReference.child("Allergies").child(MainActivity.loggedInUser.userID).setValue(dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(currentContext, "Adding allergy successful!", Toast.LENGTH_LONG).show();
                                allergyOrDislikeField.setText("");
                            }
                            else{
                                Toast.makeText(currentContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }
}