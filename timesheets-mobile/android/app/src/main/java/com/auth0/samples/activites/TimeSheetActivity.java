package com.auth0.samples.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.jwt.JWT;
import com.auth0.samples.R;
import com.auth0.samples.models.User;
import com.auth0.samples.utils.CredentialsManager;
import com.auth0.samples.utils.TimeSheetAdapter;
import com.auth0.samples.models.TimeSheet;
import com.auth0.samples.utils.UserProfileManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ej on 7/9/17.
 */

public class TimeSheetActivity extends AppCompatActivity {

    private ArrayList<TimeSheet> timesheets = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timesheet_activity);
        Toolbar navToolbar = (Toolbar) findViewById(R.id.navToolbar);
        setSupportActionBar(navToolbar);

        callAPI();
    }

    private void callAPI() {

        final Request.Builder reqBuilder = new Request.Builder()
                .get()
                .url(getString(R.string.api_url))
                .addHeader("Authorization", "Bearer " + CredentialsManager.getCredentials(this).getAccessToken());

        OkHttpClient client = new OkHttpClient();
        Request request = reqBuilder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("API", "Error: ", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TimeSheetActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                timesheets = processResults(response);
                final TimeSheetAdapter adapter = new TimeSheetAdapter(TimeSheetActivity.this, timesheets);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            ListView listView = (ListView) findViewById(R.id.timesheetList);
                            listView.setAdapter(adapter);
                            adapter.addAll(timesheets);
                        } else {
                            Toast.makeText(TimeSheetActivity.this, "API call failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private ArrayList<TimeSheet> processResults (Response response) {
        ArrayList<TimeSheet> timesheets = new ArrayList<>();
        try {
            String jsonData = response.body().string();
            if (response.isSuccessful()) {
                JSONArray timesheetJSONArray = new JSONArray(jsonData);
                for (int i = 0; i < timesheetJSONArray.length(); i++) {
                    JSONObject timesheetJSON = timesheetJSONArray.getJSONObject(i);
                    String userID = timesheetJSON.getString("user_id");
                    String projectName = timesheetJSON.getString("project");
                    String dateStr = timesheetJSON.getString("date");
                    Double hours = timesheetJSON.getDouble("hours");
                    int id = timesheetJSON.getInt("id");

                    TimeSheet timesheet = new TimeSheet(userID, projectName, dateStr, hours, id);
                    timesheets.add(timesheet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return timesheets;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timesheet_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startActivity(new Intent(TimeSheetActivity.this, FormActivity.class));
                break;
            case R.id.action_profile:
                startActivity(new Intent(TimeSheetActivity.this, UserActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
