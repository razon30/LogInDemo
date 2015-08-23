package com.example.razon30.logindemo;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;
import java.util.Date;

public class Google_Plus_Demo extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    int RC_SIGN_IN = 0;
    String TAG = "Google_Plus_Demo";
    int PROFILE_PIC_SIZE = 400;
    GoogleApiClient googleApiClient;
    boolean intentProgress;
    boolean signInClicked;
    ConnectionResult connectionResult;

    SignInButton signInButton;
    Button btnSignOut,btnRevokeAccess;
    ImageView imgProfilePic;
    TextView txtName,txtEmail;
    LinearLayout llProfileLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google__plus__demo);

        signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);

        signInButton.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (!result.hasResolution()){
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),this,RC_SIGN_IN).show();
            return;
        }
        if (!intentProgress){
            connectionResult = result;
            if (signInClicked){
                resolveSignInErrot();
            }
        }

    }

    private void resolveSignInErrot(){
        if (connectionResult.hasResolution()){
            intentProgress = true;
            try {
                connectionResult.startResolutionForResult(this,RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                intentProgress = false;
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN){
            if (resultCode != RESULT_OK){
                signInClicked = false;
            }
            intentProgress = false;
            if (!googleApiClient.isConnecting()){
                googleApiClient.connect();
            }
        }
    }
    @Override
    public void onConnected(Bundle arg0) {
        signInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

        // Update the UI after signin
        updateUI(true);

    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            signInButton.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {

        try{

            if (Plus.PeopleApi.getCurrentPerson(googleApiClient)!= null){
                Person cureentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                String personName = cureentPerson.getBirthday();
                String personPhotoUrl = cureentPerson.getImage().getUrl();
                String personGooglePlusProfile = cureentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(googleApiClient);
                txtName.setText(personName);
                txtEmail.setText(email);

                personPhotoUrl = personPhotoUrl.substring(0,personPhotoUrl.length()-2)
                        +PROFILE_PIC_SIZE;

                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
            }else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
                e.printStackTrace();
            }

    }

    @Override
    public void onConnectionSuspended(int i) {

        googleApiClient.connect();
        updateUI(false);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;
            case R.id.btn_revoke_access:
                // Revoke access button clicked
                revokeGplusAccess();
                break;
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!googleApiClient.isConnecting()) {
            signInClicked = true;
            resolveSignInErrot();
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            googleApiClient.disconnect();
            googleApiClient.connect();
            updateUI(false);
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            googleApiClient.connect();
                            updateUI(false);
                        }

                    });
        }
    }


    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_google__plus__demo, menu);
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
            Intent intent = new Intent(Google_Plus_Demo.this,Gmail_Demo.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }






}
