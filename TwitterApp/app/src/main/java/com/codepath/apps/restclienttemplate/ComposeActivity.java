package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.Locale;

import okhttp3.Headers;

import static java.lang.String.format;

public class ComposeActivity extends AppCompatActivity {
  public static final String TAG = "ComposeActivity";
  public static final int MAX_TWEET_LENGTH = 280;

  EditText etCompose;
  Button btnTweet;
  TextView textCounter;

  TwitterClient client;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_compose);
    client = TwitterApp.getRestClient(this);
    etCompose = findViewById(R.id.etCompose);
    btnTweet = findViewById(R.id.btnTweet);
    textCounter = findViewById(R.id.textCounter);


    etCompose.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }
      // Updating text counter
      //NOTE: This disables button and changes text to red when char count > 280
      @Override
      public void afterTextChanged(Editable s) {
        int textCount = s.toString().length();
        textCounter.setText(format(Locale.US,"%d/280", textCount));
        if (textCount > 280){
          Log.i(TAG, "Text has gone over the limit!");
          btnTweet.setEnabled(false);
          etCompose.setTextColor(Color.RED);
        } else {
          if(!btnTweet.isEnabled()) {
            btnTweet.setEnabled(true);
            etCompose.setTextColor(Color.BLACK);
          }
        }
      }
    });

    // Creating click listener on button
    btnTweet.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {

        final String tweetContent = etCompose.getText().toString();
        if (tweetContent.isEmpty() == true){
          Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
          return;
        }
        if (tweetContent.length() > MAX_TWEET_LENGTH){
          Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
          return;
        }
        Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
        // Making API call with text
        client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
          @Override
          public void onSuccess(int statusCode, Headers headers, JSON json) {
            Log.i(TAG, "onSuccess to publish tweet");
            try {
              Tweet tweet = Tweet.fromJson(json.jsonObject);
              Log.i(TAG, "Published tweet says:" + tweet.body);
              Intent intent = new Intent();
              intent.putExtra("tweet", Parcels.wrap(tweet));
              setResult(RESULT_OK, intent);
              // Closing the activity, passing data to parent
              finish();

            } catch (JSONException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
            Log.e(TAG, "onFailure to publish tweet", throwable);
          }
        });

      }
    });




  }
}