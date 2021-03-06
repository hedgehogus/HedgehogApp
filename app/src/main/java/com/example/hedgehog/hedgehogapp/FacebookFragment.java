package com.example.hedgehog.hedgehogapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by hedgehog on 07.10.2016.
 */

public class FacebookFragment extends Fragment {

    MainActivity mainActivity;

    LoginButton loginButton;
    CallbackManager callbackManager;
    AccessToken accessToken;
    Profile profile;
    AccessTokenTracker accessTokenTracker;

    ImageView imageView;
    TextView tvFirstName, tvLastName, tvGender, tvBirthday, tvRelationship, tvLocation;
    LinearLayout rootLayout;
    FrameLayout buttonContainer;

    final String GENDER = "gender";
    final String BIRTHDAY = "birthday";
    final String RELATIONSHIP_STATUS = "relationship_status";
    final String LOCATION = "location";
    final String NAME = "name";
    final String UNKNOWN = "unknown";

    String gender;
    String birthday;
    String relationship_status;
    String[] locationName;
    String pictureUrlString;

    Bitmap bitmap;

    AsyncTask<Integer, Void, Integer> at;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = AccessToken.getCurrentAccessToken();
        profile = Profile.getCurrentProfile();
        locationName = new String[2];
        locationName[0] = " ";
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash, container, false);

        rootLayout = (LinearLayout) view.findViewById(R.id.rootLayout);
        rootLayout.setAlpha(0);
        rootLayout.setTranslationY(-300f);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        tvFirstName = (TextView) view.findViewById(R.id.tvName);
        tvLastName = (TextView) view.findViewById(R.id.tvLastName);
        tvGender = (TextView) view.findViewById(R.id.tvGender);
        tvBirthday = (TextView) view.findViewById(R.id.tvBirthday);
        tvRelationship = (TextView) view.findViewById(R.id.tvRelationship_status);
        tvLocation = (TextView) view.findViewById(R.id.tvLocation);

        buttonContainer = (FrameLayout) view.findViewById(R.id.buttonContainer);

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email", "user_location", "user_birthday", "user_relationships");


        accessToken = AccessToken.getCurrentAccessToken();

        profile = Profile.getCurrentProfile();
        if (profile != null) {
            setInformation(profile);
        }
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null) {

                    buttonContainer.animate().translationY(0).setDuration(300);
                    mainActivity.mapContainer.animate().scaleY(0.3f).scaleX(0.3f).alpha(0).setDuration(250);
                    rootLayout.animate().translationY(-300).alpha(0).setDuration(250);
                }
            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    setInformation(currentProfile);
                }
            }
        };

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = AccessToken.getCurrentAccessToken();
                profile = Profile.getCurrentProfile();
                loginButton.setReadPermissions("user_location");
                loginButton.setReadPermissions("user_birthday");
                loginButton.setReadPermissions("user_relationships");
                //setInformation();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("error", error.getMessage());
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }
    }

    private void setInformation(Profile profile) {

        tvFirstName.setText(profile.getFirstName());
        tvLastName.setText(profile.getLastName());

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,birthday,location,relationship_status,gender");
        at = new MyAsyncTask();
        pictureUrlString = profile.getProfilePictureUri(200, 200).toString();
        //at.execute();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me?",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
                        JSONObject obj = response.getJSONObject();
                        String location = " ";
                        if (obj != null) {

                            gender = obj.optString(GENDER);
                            birthday = obj.optString(BIRTHDAY);
                            birthday = birthday.replace("/", ".");
                            relationship_status = obj.optString(RELATIONSHIP_STATUS);
                            JSONObject locationObj = obj.optJSONObject(LOCATION);
                            if (locationObj == null) {
                                location = UNKNOWN;
                            } else {
                                location = locationObj.optString(NAME);
                            }

                            locationName = location.split(", ");
                            Log.d ( "Asdf", " " + location);
                        }
                        tvGender.setText(gender);
                        tvBirthday.setText(birthday);
                        tvRelationship.setText(relationship_status);
                        tvLocation.setText(location);
                        at.execute();
                    }
                }
        ).executeAsync();
    }

    public class MyAsyncTask extends AsyncTask<Integer, Void, Integer> {

        final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        final String KEY = "AIzaSyC1OhzJh_w5YRXa3m2KL6hdKjKaXBGc4UE";
        final String RESULTS = "results";
        final String GEOMETRY = "geometry";
        final String LOCATION = "location";
        final String LAT = "lat";
        final String LNG = "lng";

        double lat;
        double lng;

        @Override
        protected Integer doInBackground(Integer... params) {
            String responce = null;
            InputStream is = null;
            String myurl = GEOCODING_URL + locationName[0] + "&key=" + KEY;
            try {
                URL url = new URL(myurl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60000 /* milliseconds */);
                conn.setConnectTimeout(60000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Log.d("error", "" + responseCode);
                }
                is = conn.getInputStream();

                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();

                String read = br.readLine();
                while (read != null) {
                    sb.append(read);
                    read = br.readLine();
                }
                responce = sb.toString();
                isr.close();
                br.close();

                JSONObject jsonResponce = null;
                try {
                    jsonResponce = new JSONObject(responce);
                } catch (JSONException e) {
                    Log.d("error", e.getMessage());
                }

                JSONArray jsonObjects = jsonResponce.optJSONArray(RESULTS);
                JSONObject jsonObject = jsonObjects.optJSONObject(0);
                JSONObject location = jsonObject.optJSONObject(GEOMETRY).optJSONObject(LOCATION);
                lat = location.optDouble(LAT);
                lng = location.optDouble(LNG);
            } catch (Exception e) {
                Log.d("error", "" + e.getMessage());}
            try {

                URL pictureUrl = new URL(pictureUrlString);

                HttpURLConnection pictureConn = (HttpURLConnection) pictureUrl.openConnection();
                pictureConn.setReadTimeout(100000);
                pictureConn.setConnectTimeout(150000);
                pictureConn.setRequestMethod("GET");
                pictureConn.setDoInput(true);
                pictureConn.connect();
                InputStream pictureIS = pictureConn.getInputStream();
                bitmap = null;
                if (isCancelled()) return 0;
                try {
                    bitmap = BitmapFactory.decodeStream(pictureIS);
                } catch (Exception e) {
                    bitmap = BitmapFactory.decodeResource(mainActivity.getResources(),
                            R.drawable.default_picture);
                }
            } catch (Exception e) {
                Log.d("error", "" + e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.d("error", e.getMessage());
                    }
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {

            if (mainActivity != null) {
                if (locationName[0] != UNKNOWN && locationName[0] != " ") {
                    LatLng mark = new LatLng(lat, lng);
                    mainActivity.mMap.clear();
                    mainActivity.mMap.addMarker(new MarkerOptions().position(mark).title(locationName[0]));
                    mainActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 6));
                }

                mainActivity.mapContainer.animate().scaleX(1).scaleY(1).alpha(1).setDuration(250);
                WindowManager wm = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int height = size.y;

                buttonContainer.animate().translationY(height - loginButton.getHeight() * 5).setDuration(300);
            }
            imageView.setImageBitmap(bitmap);
            rootLayout.animate().y(0).alpha(1).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    rootLayout.setTranslationY(0);
                    rootLayout.setAlpha(1);
                }
            });
        }
    }
}
