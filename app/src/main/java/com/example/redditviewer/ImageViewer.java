package com.example.redditviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.HashMap;

public class ImageViewer extends AppCompatActivity {
    HashMap<String, String[]> img_srcs = new HashMap<>();
    String url;
    String urlLink;
    int limit;

    ImageView contentDisplay;
    TextView contentTitle;
    Button backButton;
    ProgressBar progressBar_cyclic;
    TextView rightArrow, leftArrow;
    TextView linkLabel;

    Runnable myRunnable;

    int delay = 10000;
    int imageCount = 0;
    int imageCountLimit = 0;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        progressBar_cyclic = findViewById(R.id.progressBar_cyclic);
        progressBar_cyclic.setVisibility(View.VISIBLE);

        transitionData data = (transitionData)getIntent().getSerializableExtra("data");
        limit = data.getLimit();
        url = data.getURL()+"?limit="+limit;
        delay = data.getDelay();
        Log.v("URL", data.getURL());

        rightArrow = findViewById(R.id.rightArrow);
        leftArrow = findViewById(R.id.leftArrow);
        contentDisplay = findViewById(R.id.contentDisplay);
        contentTitle = findViewById(R.id.titleLabel);
        backButton = findViewById(R.id.backButton);
        linkLabel = findViewById(R.id.linkLabel);

        getImages();

        linkLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent urlIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://"+urlLink));
                startActivity(urlIntent);
            }}
        );

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCount-=2;
                getImage();
                restart();
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCount++;
                getImage();
                restart();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(ImageViewer.this, MainActivity.class);
                handler.removeMessages(0);
                finish();
                startActivity(mainIntent);
            }
        });
    }
    private void stopActivity(){
        finish();
    }
    private void getImages(){
        RequestQueue imageQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String img_url, img_title, post_url;
                        try{
                            /*
                            Log.v("Data", response.getJSONObject("data").getJSONArray("children")
                                    .getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                                    .getJSONObject(0).getJSONObject("variants").getJSONObject("gif").getJSONObject("source").get("url").toString().replaceAll("&amp;", "&"));


                            Log.v("Data", String.valueOf(response.getJSONObject("data").getJSONArray("children")
                                    .getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                                    .getJSONObject(0).getJSONObject("variants").length()));
                            */

                            for(int x = 0; x<limit; x++){
                                img_url = post_url = "";
                                String infoArray[] = null;
                                try{
                                    /*
                                    if(response.getJSONObject("data").getJSONArray("children")
                                            .getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                                            .getJSONObject(0).getJSONObject("variants").length()!=0){
                                        img_url=response.getJSONObject("data").getJSONArray("children")
                                                .getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                                                .getJSONObject(0).getJSONObject("variants").getJSONObject("gif").getJSONObject("source").get("url").toString().replaceAll("&amp;", "&");
                                        gif="true";
                                    }
                                    */
                                    img_url = (response.getJSONObject("data").getJSONArray("children")
                                    .getJSONObject(x).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                                    .getJSONObject(0).getJSONObject("source").get("url").toString().replace("&amp;", "&"));
                                    post_url = "reddit.com" +
                                            response.getJSONObject("data").getJSONArray("children").getJSONObject(x).getJSONObject("data").get("permalink").toString();
                                }catch(Exception err){
                                    Log.e("ImgNotFound", err.toString());
                                }
                                img_title =(response.getJSONObject("data").getJSONArray("children")
                                        .getJSONObject(x).getJSONObject("data").get("title").toString());
                                infoArray = new String[]{img_url, post_url};
                                if(!img_url.equals("")){
                                    img_srcs.put(img_title, infoArray);
                                    imageCountLimit++;
                                }
                            }
                            Log.v("IMGLIMIT", String.valueOf(imageCountLimit));
                            Log.v("Imgs", img_srcs.toString());
                            startTimer();;
                        }catch(Exception err){
                            Log.v("Error", err.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("Err", error.toString());
                    }
                });
        imageQueue.add(jsonObjectRequest);
    }

    public void restart() {
        handler.removeCallbacks(myRunnable);
        handler.postDelayed(myRunnable, delay);
    }

    private void startTimer(){
        myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.v("Elapsed Time", (String.valueOf(delay)));
                getImage();
                handler.postDelayed(this, delay);
            }
        };
        handler.postDelayed(myRunnable, delay);
    }

    private void getImage(){
        if(imageCount==0){
            progressBar_cyclic.setVisibility(View.INVISIBLE);
            leftArrow.setVisibility(View.GONE);
            rightArrow.setVisibility(View.VISIBLE);
        }else{
            leftArrow.setVisibility(View.VISIBLE);
        }
        Log.v("ImageCount", String.valueOf(imageCount));
        Log.v("ImageCountLimit", String.valueOf(imageCountLimit));
        if(imageCount==imageCountLimit) {
            handler.removeCallbacks(myRunnable);
            handler.removeCallbacksAndMessages(null);
            handler.removeMessages(0);
            stopActivity();
            imageCount++;
            return;
        }
        else if(imageCount<imageCountLimit){
            linkLabel.setText(String.valueOf(imageCount));
            Object myKey = img_srcs.keySet().toArray()[imageCount];
            urlLink = img_srcs.get(myKey)[1];
            Glide.with(this).load(img_srcs.get(myKey)[0]).into(contentDisplay);
            linkLabel.setText(img_srcs.get(myKey)[1]);
            contentTitle.setText(myKey.toString());
            imageCount++;
        }
    }
}