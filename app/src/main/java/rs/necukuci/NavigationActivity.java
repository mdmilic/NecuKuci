package rs.necukuci;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.Profile;

import java.io.IOException;
import java.net.URL;

import androidx.work.WorkManager;
import lombok.AllArgsConstructor;
import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationUploadWorker;
import rs.necukuci.user.UserManager;
import rs.necukuci.util.CrashlyticsUtil;
import rs.necukuci.util.EmulatorUtils;
import timber.log.Timber;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerFloatingActionButton();

        registerDrawer(toolbar);

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        registerSignOutBtn();

        new FacebookProfilePictureTask(getProfilePictureView()).execute();
        registerUserName();

        final String cachedUserID = UserManager.getUserID();
        CrashlyticsUtil.logUser(cachedUserID);
        if (EmulatorUtils.isEmulator()) {
            Timber.i("This is EMULATOR build, userID=%s", cachedUserID);
        } else {
            Timber.i("This is NOT EMULATOR build, userID=%s", cachedUserID);
        }

        // TODO: Can remove after stable build because of the phone
        WorkManager.getInstance().cancelAllWork();

        if (PermissionChecker.checkNetworkPermission(this)) {
            Timber.i("Starting upload worker from navigation activity");
            LocationUploadWorker.scheduleLocationUpload();
        }
    }

    // This should depend on the type of login, only FB for now
    private void registerUserName() {
        if (AccessToken.getCurrentAccessToken() != null) {
            final String facebookName = Profile.getCurrentProfile().getName();

            final TextView profileNameView = getProfileNameView();
            profileNameView.setText(facebookName);
            Timber.i("FB user logged in as %s", facebookName);
        } else {
            Timber.i("FB user not logged in");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Timber.i("camera");
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Timber.i("galery");

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void registerDrawer(final Toolbar toolbar) {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void registerFloatingActionButton() {
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","mdmilic@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "NecuKuci Travel Tracker");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });
    }

    private void registerSignOutBtn() {
        // Create log out Button on click listener
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View header = navigationView.getHeaderView(0);
        header.findViewById(R.id.signOutButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                // Cancel all scheduled work as app wont have permissions to access AWS
                WorkManager.getInstance().cancelAllWork();
                IdentityManager.getDefaultIdentityManager().signOut();
            }
        });
    }

    @AllArgsConstructor
    private static class FacebookProfilePictureTask extends AsyncTask<Void, Void, Bitmap> {
        private final ImageView imageView;

        @Override
        protected Bitmap doInBackground(final Void... voids) {
            if (AccessToken.getCurrentAccessToken() != null) {
                final String facebookUserID = Profile.getCurrentProfile().getId();
                Timber.i("FB user logged in with ID %s", facebookUserID);
                return getFacebookProfilePicture(facebookUserID);
            } else {
                Timber.i("FB user not logged in");
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Bitmap facebookPhoto) {
            super.onPostExecute(facebookPhoto);
            registerUserPhoto(facebookPhoto);
        }

        private Bitmap getFacebookProfilePicture(final String userID){
            try {
                final URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
                return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            } catch (final IOException e) {
                Crashlytics.logException(e);
                Timber.e("Failed to get users facebook photo");
            }
            return null;
        }

        // This should depend on the type of login, only FB for now
        private void registerUserPhoto(final Bitmap profilePicture) {
            if (profilePicture != null) {
                imageView.setImageBitmap(profilePicture);
            }
//            If using FB's profile picture view
//            final ProfilePictureView profilePictureView = header.findViewById(R.id.imageView);
//            profilePictureView.setProfileId(facebookUserID);
        }
    }

    private ImageView getProfilePictureView() {
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View header = navigationView.getHeaderView(0);
        return header.findViewById(R.id.imageView);
    }

    private TextView getProfileNameView() {
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View header = navigationView.getHeaderView(0);
        return header.findViewById(R.id.profileName);
    }
}
