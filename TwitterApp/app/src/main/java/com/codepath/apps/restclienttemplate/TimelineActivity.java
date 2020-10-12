package com.codepath.apps.restclienttemplate;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.codepath.apps.restclienttemplate.models.Tweet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import java.util.ArrayList;
import org.json.JSONArray;
import android.os.Bundle;
import android.util.Log;
import okhttp3.Headers;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {


  public static final String TAG = "TimelineActivity";
  TwitterClient client;
  RecyclerView rvTweets;
  List<Tweet> tweets;
  TweetsAdapter adapter;
  SwipeRefreshLayout swipeContainer;
  EndlessRecyclerViewScrollListener scrollListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timeline);
    client = TwitterApp.getRestClient(this);

    swipeContainer = findViewById(R.id.swipeContainer);
    // Configure the refreshing colors
    swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);
    // Adding Listener
    swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        Log.i(TAG, "fetching new data!");
        populateHomeTimeline();
      }
    });

    // Find the recycler view
    rvTweets = findViewById(R.id.rvTweets);

    tweets = new ArrayList<>();
    adapter = new TweetsAdapter(this, tweets);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    // Recycler view setup: layout manager and the adapter
    rvTweets.setLayoutManager(new LinearLayoutManager(this));
    rvTweets.setAdapter(adapter);

    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
      @Override
      public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
        Log.i(TAG, "onLoadMore" + page);
        loadMoreData();
      }
    };

    // Adding scroll listener to RecyclerView
    rvTweets.addOnScrollListener(scrollListener);


    populateHomeTimeline();
  }
  private void loadMoreData() {
    client.getNextPageOfTweets(new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Headers headers, JSON json) {
        Log.i(TAG, "onSuccess for load more data" + json.toString());
        JSONArray jsonArray = json.jsonArray;
        try {
          List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
          adapter.addAll(tweets);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      @Override
      public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
        Log.i(TAG, "onFailure for loadMoreData!", throwable);
      }
  }, tweets.get(tweets.size()+-1).id);

  }
  private void populateHomeTimeline() {
    client.getHomeTimeline(new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Headers headers, JSON json) {
        Log.i(TAG, "onSuccess!" + json.toString());
        JSONArray jsonArray = json.jsonArray;
        try {
          adapter.clear();
          adapter.addAll(Tweet.fromJsonArray(jsonArray));
          // Now we call setRefreshing to signal refresh has finished
          swipeContainer.setRefreshing(false);
        } catch (JSONException e) {
          Log.e(TAG, "Json exception", e);
          e.printStackTrace();
        }
      }
      @Override
      public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
        Log.i(TAG, "onFailure!" + response, throwable);
      }
    });
  }
}
