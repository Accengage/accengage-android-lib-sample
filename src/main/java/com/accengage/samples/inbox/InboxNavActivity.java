package com.accengage.samples.inbox;

import android.content.Intent;
import android.graphics.Typeface;
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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.auth.AuthActivity;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.base.BaseActivity;
import com.accengage.samples.firebase.Constants;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.fragment.InboxMessagesFragment;
import com.ad4screen.sdk.Acc;
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

public class InboxNavActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, AccengageFragment.OnFragmentCreateViewListener {

    private static final String TAG = "InboxNavActivity ";
    private static final int MENU_CATEGORY_GROUP_ID = 123;

    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseUser mCurrentUser;
    private InboxMessage mClickedMessage;
    private boolean mIsArchived = false;
    private String mLabel = Constants.Inbox.Messages.PRIMARY;

    private MessagesHandler mMessageHandler = new MessagesHandler();
    private Menu mNavigationMenu;
    private SubMenu mCategoryMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_nav);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavigationMenu = navigationView.getMenu();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            Intent intent = AuthActivity.createIntent(this, this.getPackageName());
            startActivity(intent);
            finish();
            return;
        }

        InboxMessagesManager.get(getApplicationContext()).subscribeForMessages(mMessageHandler);
        displayFragment(InboxMessagesFragment.class);
    }

    private View.OnClickListener mNavigationBackPressListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                if (!isFinishing()) {
                    finish();
                }
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

        if (id == R.id.nav_inbox_primary) {
            mIsArchived = false;
            mLabel = Constants.Inbox.Messages.PRIMARY;
            replaceFragment(InboxMessagesFragment.class);
        } else if (id == R.id.nav_inbox_archive) {
            mIsArchived = true;
            mLabel = Constants.Inbox.Messages.PRIMARY;
            replaceFragment(InboxMessagesFragment.class);
        } else if (id == R.id.nav_inbox_trash) {
            mLabel = Constants.Inbox.Messages.TRASH;
            replaceFragment(InboxMessagesFragment.class);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InboxMessagesManager.get(getApplicationContext()).unsubscribeFromMessages(mMessageHandler);
    }

    public void replaceFragment(Class fragmentClass) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        displayFragment(fragmentClass);
    }

    public void displayFragment(Class fragmentClass) {
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

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.flContent, fragment, backStateName);
        ft.addToBackStack(backStateName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onFragmentCreateViewDone(AccengageFragment fragment) {
        // Update the navigation bar title
        if (fragment instanceof AccengageFragment) {
            String viewName =  fragment.getViewName(this);
            if (viewName != null) {
                Acc.get(this).setView(viewName);
                setTitle(viewName);
                //checkMenuItemWithName(viewName);
                //mCurrentView = viewName;
            }
        }

        // Update the navigation bar toggle
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            // Remove the hamburger icon
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            // Show the back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set on toggle click listener to pop the previous fragment
            mDrawerToggle.setToolbarNavigationClickListener(mNavigationBackPressListener);
        } else {
            Log.debug("andrei set toggle to default state");
            // Remove the back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show the hamburger button
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            // Set on toggle click listener to open nav drawer
            mDrawerToggle.setToolbarNavigationClickListener(mDrawerToggle.getToolbarNavigationClickListener());
        }
    }

    public void setClickedMessage(InboxMessage message) {
        mClickedMessage = message;
    }

    public InboxMessage getClickedMessage() {
        return mClickedMessage;
    }

    public boolean isArchived() {
        return mIsArchived;
    }

    public String getLabel() {
        return mLabel;
    }


    private class MessagesHandler extends DisposableObserver<Message> {

        private boolean mIsAllMessagesReceived = false;
        private int mReceivedMessageCount = 0;
        private int mHandledMessageCount = 0;

        private abstract class CategoryEventListener {
            abstract void onCategoryDone();
        }

        @Override
        public void onNext(@NonNull Message message) {
            String uid = mCurrentUser.getUid();
            final InboxMessage inboxMessage = new InboxMessage(message, uid);
            Log.debug(TAG + "onNext message id " + inboxMessage.id);
            mReceivedMessageCount++;
            writeMessage(inboxMessage);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.debug(TAG + "An error is occurred while getting inbox messages");
        }

        @Override
        public void onComplete() {
            Log.debug(TAG + "Getting inbox messages is done, message count: " + mReceivedMessageCount);
            mIsAllMessagesReceived = true;
            readCategoriesIfMessagesHandled();
        }

        private void writeMessage(final InboxMessage message) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(Constants.Inbox.Messages.PRIMARY).
                    child(message.id).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    InboxMessage msgFromDB = dataSnapshot.getValue(InboxMessage.class);
                    if (msgFromDB == null) {
                        Log.debug(TAG + "Add new inbox message " + message.id);
                        dataSnapshot.getRef().setValue(message);
                        writeCategory(message, new CategoryEventListener() {
                            @Override
                            public void onCategoryDone() {
                                mHandledMessageCount++;
                                readCategoriesIfMessagesHandled();
                            }
                        });
                    } else {
                        Log.debug(TAG + "Inbox message " + msgFromDB.id + " is already existed in the DB");
                        // check if instances are equal
                        if (!message.equals(msgFromDB)) {
                            Log.debug("Update Inbox message " + msgFromDB.id);
                            Map<String, Object> msgValues = message.toMap();
                            dataSnapshot.getRef().updateChildren(msgValues);
                        }
                        mHandledMessageCount++;
                        readCategoriesIfMessagesHandled();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.debug(TAG + "onCancelled Inbox message " + databaseError);
                }
            });
        }

        private void writeCategory(final InboxMessage inboxMessage, final CategoryEventListener listener) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

            if (!TextUtils.isEmpty(inboxMessage.category)) {

                dbRef.child(Constants.USER_INBOX_CATEGORIES).child(inboxMessage.uid).child(inboxMessage.category)
                        .child(inboxMessage.id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String id = dataSnapshot.getValue(String.class);
                        if (id == null) {
                            Log.debug(TAG + "Add a message " + inboxMessage.id + " to category " + inboxMessage.category);
                            dataSnapshot.getRef().setValue(inboxMessage.id);
                        } else {
                            Log.debug(TAG + "A message " + inboxMessage.id + " is already existed in category " + inboxMessage.category);
                        }
                        listener.onCategoryDone();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.debug(TAG + "onCancelled Inbox category " + databaseError);
                        listener.onCategoryDone();
                    }
                });
            } else {
                listener.onCategoryDone();
            }
        }

        private void readCategoriesIfMessagesHandled() {
            if (!mIsAllMessagesReceived)
                return;

            if (mReceivedMessageCount == mHandledMessageCount) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child(Constants.USER_INBOX_CATEGORIES).child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        createCategoryMenu();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String categoryName = child.getKey();
                            long messagesCount = child.getChildrenCount();
                            Log.debug(TAG + "Category " + categoryName + " has " + messagesCount + " message(s)");
                            populateCategoryMenuItem(categoryName, messagesCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }

    private void createCategoryMenu() {
        if (mCategoryMenu != null) {
            mNavigationMenu.removeGroup(MENU_CATEGORY_GROUP_ID);
        }
        mCategoryMenu = mNavigationMenu.addSubMenu(MENU_CATEGORY_GROUP_ID, Menu.NONE, Menu.NONE, R.string.nav_inbox_categories);
    }
    private void populateCategoryMenuItem(String category, long count) {
        MenuItem item = mCategoryMenu.add(category);
        item.setIcon(R.drawable.ic_action_label);
        TextView tv = new TextView(this);
        tv.setText(String.valueOf(count));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        item.setActionView(tv);
        item.setCheckable(true);
    }
}
