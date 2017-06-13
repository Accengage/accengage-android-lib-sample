package com.accengage.samples.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.accengage.samples.R;
import com.accengage.samples.auth.AuthActivity;
import com.accengage.samples.base.BaseActivity;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.fragment.InboxMessagesFragment;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public class InboxNavActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            Intent intent = AuthActivity.createIntent(this, this.getPackageName());
            startActivity(intent);
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        InboxMessagesManager.get(getApplicationContext()).subscribeForMessages(mCallback);

        displayFragment(InboxMessagesFragment.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!isFinishing()) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.inbox_nav, menu); TODO add search item and replace the icon
        //return true;
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) { // TODO replace by search function
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InboxMessagesManager.get(getApplicationContext()).unsubscribeFromMessages(mCallback);
    }

    protected void displayFragment(Class fragmentClass) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String backStateName = fragmentClass.getName();
        Fragment fragment = fragmentManager.findFragmentByTag(backStateName);
        if (fragment == null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (InstantiationException e) {
                return;
            } catch (IllegalAccessException e) {
                return;
            }
        }
//        if (fragment instanceof AccengageFragment) {
//            String viewName = ((AccengageFragment) fragment).getViewName(this);
//            if (viewName != null) {
//                A4S.get(this).setView(viewName);
//                setTitle(viewName);
//                checkMenuItemWithName(viewName);
//                mCurrentView = viewName;
//            }
//        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.flContent, fragment, backStateName);
        ft.addToBackStack(backStateName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private DisposableObserver<Message> mCallback = new DisposableObserver<Message>() {

        @Override
        public void onNext(@NonNull Message message) {
            String uid = mCurrentUser.getUid();
            final InboxMessage inboxMessage = new InboxMessage(message, uid, mCurrentUser.getDisplayName());
            Log.debug("onNext message id " + inboxMessage.id);

            mDatabase.child("user-inbxmessages").child(uid).child(inboxMessage.id).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    InboxMessage msgFromDB = dataSnapshot.getValue(InboxMessage.class);
                    if (msgFromDB == null) {
                        Log.debug("Add new inbox message " + inboxMessage.id);
                        dataSnapshot.getRef().setValue(inboxMessage);
                    } else {
                        Log.debug("Inbox message " + msgFromDB.id + " is already existed in the DB");
                        // check if instances are equal
                        if (!inboxMessage.equals(msgFromDB)) {
                            Log.debug("Update Inbox message " + msgFromDB.id);
                            Map<String, Object> msgValues = inboxMessage.toMap();
                            dataSnapshot.getRef().updateChildren(msgValues);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.debug("onCancelled Inbox message " + databaseError);
                }
            });
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.debug("An error is occurred while getting inbox messages");
        }

        @Override
        public void onComplete() {
            Log.debug("Getting inbox messages is done");
        }
    };
}
