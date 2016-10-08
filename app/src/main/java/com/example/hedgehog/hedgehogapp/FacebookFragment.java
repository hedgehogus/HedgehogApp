package com.example.hedgehog.hedgehogapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.MapView;

import org.json.JSONObject;

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
    TextView tvFirstName, tvLastName;

    final String GENDER = "gender";
    final String BIRTHDAY = "birthday";
    final String RELATIONSHIP_STATUS = "relationship_status";

    String gender;
    String birthday;
    String relationship_status;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = AccessToken.getCurrentAccessToken();
        profile = Profile.getCurrentProfile();
    }

    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash, container, false);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        tvFirstName = (TextView) view.findViewById(R.id.tvName);
        tvLastName = (TextView) view.findViewById(R.id.tvLastName);

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email");

        accessToken = AccessToken.getCurrentAccessToken();

        profile = Profile.getCurrentProfile();
        if (profile!= null) {
            setInformation();
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = AccessToken.getCurrentAccessToken();
                profile = Profile.getCurrentProfile();
                loginButton.setReadPermissions("user_location");
                loginButton.setReadPermissions("user_birthday");
                loginButton.setReadPermissions("user_relationships");
                setInformation();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager!= null) {
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

    private void setInformation() {
        tvFirstName.setText(profile.getFirstName());
        tvLastName.setText(profile.getLastName());

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,birthday,location,relationship_status,gender" );

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me?",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
                        JSONObject obj = response.getJSONObject();

                        gender = obj.optString(GENDER);
                        birthday = obj.optString(BIRTHDAY);
                        birthday = birthday.replace("\\", ".");



                        Log.d("asdf", " " + obj);
                        Log.d("asdf", " " + birthday);


                    }
                }
        ).executeAsync();

    }
}
