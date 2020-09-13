package com.example.redditviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Arrays;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {
    String subReddit = null;
    String identifier = null;

    TextView errorMessage;
    EditText subRedditElement;
    Spinner identifierElement;
    EditText delay;

    String [] spinnerItems;

    int delayInt;
    int limit;

    String baseURL = "https://www.reddit.com/r/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner dropdown = findViewById(R.id.spinner1);
        spinnerItems = new String[]{"Top", "Hot", "New"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        dropdown.setAdapter(adapter);

        Button goButton = findViewById(R.id.GoButton);
        subRedditElement = findViewById(R.id.subredditInput);
        delay = findViewById(R.id.delayButton);
        errorMessage = findViewById(R.id.errorMsg);

       TextWatcher watcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                hideError();
                String strEnteredVal = delay.getText().toString();
                try{
                    if(!strEnteredVal.equals("")) {
                        int num = Integer.parseInt(strEnteredVal);
                        if (num > 60 || num<1) {
                            delay.setText("");
                        }
                    }
                }catch(Exception err){
                    delay.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        subRedditElement.addTextChangedListener(watcher);
        delay.addTextChangedListener(watcher);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeElements();
                if(assertElementValues()){
                    findReddit();
                }
                else{
                    displayError();
                }
            }
        });
    }

    private void initializeElements(){
        subReddit = subRedditElement.getText().toString();
        try{
            delayInt = parseInt(delay.getText().toString())*1000;
        }catch(Exception err){
            delayInt = 8000;
        }
        identifierElement = findViewById(R.id.spinner1);
        identifier = identifierElement.getSelectedItem().toString();
    }

    private boolean assertElementValues(){
        Boolean log = subReddit.length() > 0 && Arrays.asList(spinnerItems).contains(identifier);
        return (subReddit.length() > 0 && Arrays.asList(spinnerItems).contains(identifier));
    }

    private void displayError(){
        errorMessage.setText("Could not find any posts!");
        errorMessage.setVisibility(View.VISIBLE);
    }

    private void hideError(){
        errorMessage.setVisibility(View.INVISIBLE);
    }

    private void findReddit(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = baseURL+subReddit+"/"+identifier.toLowerCase()+".json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url+"?limit=100", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            limit = response.getJSONObject("data").getJSONArray("children").length();
                            Log.v("Limit", String.valueOf(limit));
                            if(response.getJSONObject("data").getJSONArray("children").length() == 0){
                                displayError();
                            }
                            else{
                                transitionData data = new transitionData(url, delayInt,limit);
                                Intent imageIntent = new Intent(MainActivity.this, ImageViewer.class);
                                imageIntent.putExtra("data", data);
                                startActivity(imageIntent);
                            }
                        }catch(Exception err){
                            displayError();
                            Log.v("Error", err.toString());
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        displayError();
                        Log.v("Err", error.toString());

                    }
                });
        queue.add(jsonObjectRequest);
    }
}