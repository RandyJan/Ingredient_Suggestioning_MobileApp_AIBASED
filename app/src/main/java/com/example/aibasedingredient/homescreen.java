package com.example.aibasedingredient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class homescreen extends AppCompatActivity {

    ActivityHomescreenBinding homeScreemAct;
    Context currentContext;
    LinearLayout drawerView;
    Dialog popUpDialog;
    EditText allergyOrDislikeField;
    List<String> currentUserDislikes;
    List<String> currentUserAllergies;
    TextView usernameView;
    TextView emailView;
    TextView dislikeView;
    TextView allergiesView;
    EditText queryField;
    GenerativeModel generativeModel;
    GenerativeModelFutures model;
    LinearLayout queryDataExchange;

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

        usernameView = homeScreemAct.drawerUsernameText;
        emailView = homeScreemAct.drawerEmailText;
        dislikeView = homeScreemAct.drawerDislikesText;
        allergiesView = homeScreemAct.drawerAllergiesText;
        ImageButton addDislikeButton = homeScreemAct.addDislikesButton;
        addDislikeButton.setOnClickListener(c->showDialogForAddDislike());
        ImageButton addAllergyButton = homeScreemAct.addAllergicToButton;
        addAllergyButton.setOnClickListener(c->showDialogForAddAllergy());
        ImageButton logoutButton = homeScreemAct.logoutButton;
        logoutButton.setOnClickListener(c->LogoutUser());

        ImageButton removeDislike = homeScreemAct.removeDislikeButton;
        ImageButton removeAllergy = homeScreemAct.removeAllergyButton;
        removeDislike.setOnClickListener(c->showDialogForRemovingDislike());
        removeAllergy.setOnClickListener(c->showDialogForRemovingAllergy());


        SetUserInfoInDrawer();

        popUpDialog = new Dialog(this);
        popUpDialog.setContentView(R.layout.addingredient_popup);
        allergyOrDislikeField = popUpDialog.findViewById(R.id.allergyOrDislikeField);
        ImageButton closeDialogButton = popUpDialog.findViewById(R.id.closeAddingDialog);
        closeDialogButton.setOnClickListener(c->popUpDialog.dismiss());

        currentUserDislikes = new ArrayList<>();
        currentUserAllergies = new ArrayList<>();

        GetAndUpdateDislikes();
        GetAndUpdateAllergies();

        generativeModel = new GenerativeModel("gemini-1.5-flash", "AIzaSyAZjpp2l9qpZEEj4s07DLJiqLHRV_S80zE");
        model = GenerativeModelFutures.from(generativeModel);

        queryField = homeScreemAct.QueryEditText;
        Button confirmQueryButton = homeScreemAct.confirmQueryButton;
        confirmQueryButton.setOnClickListener(c->StartQuery());

        queryDataExchange = homeScreemAct.queryExchangeData;
    }

    private String getDislikeString(){
        StringBuilder updatedDislike = new StringBuilder();
        int dislikeCount = currentUserDislikes.size();
        int count = 0;
        for(String data : currentUserDislikes){
            count++;
            String sign = (count < dislikeCount) ? "," : ".";
            updatedDislike.append(data).append(sign);
        }
        return updatedDislike.toString();
    }
    private String getAlleriesString(){
        StringBuilder updatedAllergy = new StringBuilder();
        int allergyCount = currentUserAllergies.size();
        int count = 0;
        for(String data : currentUserAllergies){
            count++;
            String sign = (count < allergyCount) ? "," : "";
            updatedAllergy.append(data).append(sign);
        }
        return updatedAllergy.toString();
    }

    private void StartQuery(){
        String query = String.valueOf(queryField.getText());
        String allergies = getAlleriesString();
        String dislikes = getDislikeString();
        String fullQueryString = "Give substitute for " + query + " for person with allergies of " + allergies + " and dislikes " + dislikes + ". Response should not have explanation.";

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View userQuery = inflater.inflate(R.layout.reusable_user_query_view, null);
        TextView userMessage = userQuery.findViewById(R.id.userMessage);
        userMessage.setText(query);
        queryDataExchange.addView(userQuery);
        queryField.setText("");


        Content content = new Content.Builder().addText(fullQueryString).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultString = result.getText().replaceAll("\n", ",").replaceAll("-", "").replaceAll("\\*", "");
                Log.d("QUERY RESULT", resultString);
                List<String> suggestions = new ArrayList<>();
                suggestions = Arrays.stream(resultString.split(",")).collect(Collectors.toList());

                StringBuilder fullResult = new StringBuilder();
                int suggestionCount = suggestions.size();
                int loopCount = 0;
                for(String item : suggestions){
                    loopCount++;
                    if(loopCount < suggestionCount){
                        fullResult.append(item).append(",");
                    }
                    else{
                        fullResult.append(" and").append(item);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View AIResponse = inflater.inflate(R.layout.reusable_ai_response_view, null);
                        TextView responseView = AIResponse.findViewById(R.id.queryResponse);
                        responseView.setText(fullResult.toString());
                        queryDataExchange.addView(AIResponse);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(currentContext, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, executor);
    }

    private void SetUserInfoInDrawer(){
        String usernameDefaultString = String.valueOf(usernameView.getText()) + " " + MainActivity.loggedInUser.username;
        usernameView.setText(usernameDefaultString);

        String userEmailDefaultString = String.valueOf(emailView.getText()) + " " + MainActivity.loggedInUser.userEmail;
        emailView.setText(userEmailDefaultString);
    }

    private void GetAndUpdateDislikes(){
        MainActivity.databaseReference.child("Dislikes").child(MainActivity.loggedInUser.userID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("data").exists()){
                    String dislikes = String.valueOf(snapshot.child("data").getValue());
                    Log.d(null, "dislikes are " + dislikes);
                    currentUserDislikes = Arrays.stream(dislikes.split(",")).collect(Collectors.toList());
                    String updatedDislike = getDislikeString();
                    String stringView = "Dislikes: " + updatedDislike;
                    dislikeView.setText(stringView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void GetAndUpdateAllergies(){
        MainActivity.databaseReference.child("Allergies").child(MainActivity.loggedInUser.userID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("data").exists()){
                    String dislikes = String.valueOf(snapshot.child("data").getValue());
                    currentUserAllergies = Arrays.stream(dislikes.split(",")).collect(Collectors.toList());

                    /*StringBuilder updatedAllergies = new StringBuilder();
                    int allergyCount = currentUserAllergies.size();
                    int count = 0;
                    for(String data : currentUserAllergies){
                        count++;
                        String sign = (count < allergyCount) ? "," : ".";
                        Log.d("Allergies", data);
                        updatedAllergies.append(data).append(sign);
                    }*/
                    String updatedAllergies = getAlleriesString();
                    String stringView = "Allergic to: " + updatedAllergies;
                    allergiesView.setText(stringView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void showDialogForRemovingDislike(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        TextView dialogTitle = popUpDialog.findViewById(R.id.addOrRemoveTitle);
        dialogTitle.setText("Remove Ingredient");
        confirmAdding.setOnClickListener(c->RemoveDislike());
        popUpDialog.show();
    }

    private void showDialogForRemovingAllergy(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        TextView dialogTitle = popUpDialog.findViewById(R.id.addOrRemoveTitle);
        dialogTitle.setText("Remove Ingredient");
        confirmAdding.setOnClickListener(c->RemoveAllergy());
        popUpDialog.show();
    }

    private void showDialogForAddDislike(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        TextView dialogTitle = popUpDialog.findViewById(R.id.addOrRemoveTitle);
        dialogTitle.setText("Add Ingredient");
        confirmAdding.setOnClickListener(c->AddDislike());
        popUpDialog.show();
    }

    private void showDialogForAddAllergy(){
        ImageButton confirmAdding = popUpDialog.findViewById(R.id.confirmAddIngredientButton);
        TextView dialogTitle = popUpDialog.findViewById(R.id.addOrRemoveTitle);
        dialogTitle.setText("Add Ingredient");
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
                              dislikeList = Arrays.stream(existingDislike.split(",")).collect(Collectors.toList());
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

    private void RemoveDislike(){
        String newDislike = String.valueOf(allergyOrDislikeField.getText());
        Log.d("Removed item", newDislike);
        MainActivity.databaseReference.child("Dislikes").child(MainActivity.loggedInUser.userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String data = "";
                    List<String> dislikeList = new ArrayList<>();
                    if(task.getResult().child("data").exists()){
                        String existingDislike = task.getResult().child("data").getValue().toString();

                        dislikeList = Arrays.stream(existingDislike.split(",")).collect(Collectors.toList());
                        //data = existingDislike;
                        for (String s : dislikeList) {
                            if(s.toLowerCase().equals(newDislike.toLowerCase())){
                                continue;
                            }
                            data += s + ",";
                        }
                    }

                    DislikeOrAllergyClass dislikes = new DislikeOrAllergyClass(data);
                    MainActivity.databaseReference.child("Dislikes").child(MainActivity.loggedInUser.userID).setValue(dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(currentContext, "Removing dislike successful!", Toast.LENGTH_LONG).show();
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


    private void RemoveAllergy(){
        String newDislike = String.valueOf(allergyOrDislikeField.getText());
        Log.d("Removed item", newDislike);
        MainActivity.databaseReference.child("Allergies").child(MainActivity.loggedInUser.userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String data = "";
                    List<String> dislikeList = new ArrayList<>();
                    if(task.getResult().child("data").exists()){
                        String existingDislike = task.getResult().child("data").getValue().toString();

                        dislikeList = Arrays.stream(existingDislike.split(",")).collect(Collectors.toList());

                        //data = existingDislike;
                        for (String s : dislikeList) {
                            if(s.toLowerCase().equals(newDislike.toLowerCase())){
                                continue;
                            }
                            data += s + ",";
                        }
                    }

                    DislikeOrAllergyClass dislikes = new DislikeOrAllergyClass(data);
                    MainActivity.databaseReference.child("Allergies").child(MainActivity.loggedInUser.userID).setValue(dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(currentContext, "Removing allergy successful!", Toast.LENGTH_LONG).show();
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
                        allergyList = Arrays.stream(existingAllergy.split(",")).collect(Collectors.toList());
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