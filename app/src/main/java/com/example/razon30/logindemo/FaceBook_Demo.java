package com.example.razon30.logindemo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Arrays;

public class FaceBook_Demo extends AppCompatActivity {

    CallbackManager callbackManager;
    AccessToken accessToken;
    LoginButton button;
    TextView tv_id,tv_name;
    ImageView image_pp;
    Button logout,login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_face_book__demo);


        AccessTokenTracker accessTokenTracker;
        ProfileTracker profileTracker = null;

       // logout = (Button) findViewById(R.id.logout);
        login = (Button) findViewById(R.id.logIN);
        button = (LoginButton) findViewById(R.id.login_button);
        button.setReadPermissions("user_friends");
        button.setReadPermissions("public_profile");
       // button.setVisibility(button.GONE);
        tv_id = (TextView) findViewById(R.id.tv_id);
        tv_name = (TextView) findViewById(R.id.tv_name);
        image_pp = (ImageView) findViewById(R.id.image_pp);


//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LoginManager.getInstance().logOut();
//
//
//                tv_name.setText("no name");
//                image_pp.setBackgroundResource(R.mipmap.ic_launcher);
//                Intent intent = new Intent(FaceBook_Demo.this,FaceBook_Demo.class);
//                startActivity(intent);
//
//            }
//        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_material_dark));
                    getWindow().setStatusBarColor(getResources().getColor(R.color.accent_material_dark));
                }
//                button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        accessToken = loginResult.getAccessToken();
//                        Profile profile = Profile.getCurrentProfile();
//                        displayMessage(profile);
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        // App code
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        // App code
//                    }
//                });

            }
        });


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        accessToken = loginResult.getAccessToken();
                        Profile profile = Profile.getCurrentProfile();
                        displayMessage(profile);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {




            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {

                displayMessage(profile1);

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();




        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.razon30.logindemo",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Toast.makeText(FaceBook_Demo.this,"KeyHash: "+Base64.encodeToString(md.digest(),
                        Base64.DEFAULT),Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void displayMessage(Profile profile) {



        //getting data from graph api
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Insert your code here

                        if (object!=null) {
                            try {
                                String nmae = object.getString("name");
                                tv_name.setText(nmae);
                                String id = object.getString("id");
                                tv_id.setText(id);
                                JSONObject cover = object.getJSONObject("cover");
                                String url = cover.getString("source");

                                Picasso.with(FaceBook_Demo.this).load(url).into(image_pp);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(FaceBook_Demo.this,"Log in To FaceBook",Toast
                                    .LENGTH_SHORT).show();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,cover,devices");
        request.setParameters(parameters);
        request.executeAsync();




//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/me",
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//            /* handle the result */
//
//                        JSONObject object = response.getJSONObject();
//                        if (object!=null) {
//                            try {
//                                String nmae = object.getString("name");
//                                tv_name.setText(nmae);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else {
//                            Toast.makeText(FaceBook_Demo.this,"Log in To FaceBook",Toast
//                                    .LENGTH_SHORT).show();
//                        }
//
//                    }
//                }
//        ).executeAsync();



//        if (profile != null) {
//            textView.setText(profile.getProfilePictureUri(68,68).toString());
//        } else {
//            textView.setText("Pai nai,amar dea kicchu hobe na");
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        displayMessage(profile);
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_face_book__demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(FaceBook_Demo.this,Google_Plus_Demo.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
