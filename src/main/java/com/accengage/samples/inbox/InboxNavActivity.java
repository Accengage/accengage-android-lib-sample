package com.accengage.samples.inbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.auth.AuthActivity;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.base.BaseActivity;
import com.accengage.samples.firebase.Constants;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.fragment.InboxMessagesFragment;
import com.ad4screen.sdk.A4S;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public class InboxNavActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, AccengageFragment.OnFragmentCreateViewListener {

    private static final String TAG = "InboxNavActivity ";
    private static final int MENU_CATEGORY_GROUP_ID = 123;

    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseUser mCurrentUser;
    private InboxMessage mClickedMessage;
    private String mLabel = Constants.Inbox.Messages.Label.PRIMARY;
    private String mCategory;

    private MessagesHandler mMessageHandler = new MessagesHandler();
    private Menu mNavigationMenu;
    private SubMenu mCategoryMenu;

    private Set<String> mReceivedMessageIds = new HashSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_nav);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            Intent intent = AuthActivity.createIntent(this, getClass().getName());
            startActivity(intent);
            finish();
            return;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNavigationMenu = navigationView.getMenu();

        View headerView = navigationView.getHeaderView(0);
        final ImageView accountImageView = headerView.findViewById(R.id.iv_user_icon);
        if (mCurrentUser.getPhotoUrl() != null) {
            Glide.with(this).load(mCurrentUser.getPhotoUrl()).asBitmap().into(new BitmapImageViewTarget(accountImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(InboxNavActivity.this.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    accountImageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        accountImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AuthActivity.createIntent(InboxNavActivity.this, InboxNavActivity.this.getClass().getName());
                startActivity(intent);
                finish();
                return;
            }
        });
        TextView nameView = headerView.findViewById(R.id.tv_user_name);
        nameView.setText(mCurrentUser.getDisplayName());
        TextView emailView = headerView.findViewById(R.id.tv_user_email);
        emailView.setText(mCurrentUser.getEmail());

        InboxMessagesManager.get(getApplicationContext()).subscribeForMessages(mMessageHandler);
        displayFragment(InboxMessagesFragment.class);
        navigationView.setCheckedItem(R.id.nav_inbox_primary);
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
        mCategory = null;

        if (id == R.id.nav_inbox_primary) {
            mLabel = Constants.Inbox.Messages.Label.PRIMARY;
        } else if (id == R.id.nav_inbox_archive) {
            mLabel = Constants.Inbox.Messages.Label.ARCHIVE;
        } else if (id == R.id.nav_inbox_expired) {
            mLabel = Constants.Inbox.Messages.Label.EXPIRED;
        } else if (id == R.id.nav_inbox_trash) {
            mLabel = Constants.Inbox.Messages.Label.TRASH;
        } else {
            mCategory = item.getTitle().toString();
        }
        replaceFragment(InboxMessagesFragment.class);

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
                A4S.get(this).setView(viewName);
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

    public String getLabel() {
        return mLabel;
    }

    public String getCategory() {
        return mCategory;
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
            mReceivedMessageIds.add(inboxMessage.id);
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
            checkExpiredMessagesAndReadCategories();
        }

        private void writeMessage(final InboxMessage message) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(Constants.Inbox.Messages.Box.INBOX).
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
                                readCategories();
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
                        checkExpiredMessagesAndReadCategories();
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

        private void checkExpiredMessagesAndReadCategories() {
            if (!mIsAllMessagesReceived)
                return;

            if (mReceivedMessageCount == mHandledMessageCount) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child(Constants.USER_INBOX_MESSAGES).child(mCurrentUser.getUid()).child(Constants.Inbox.Messages.Box.INBOX)
                        .orderByChild("label").equalTo(Constants.Inbox.Messages.Label.PRIMARY)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            InboxMessage msgFromDB = child.getValue(InboxMessage.class);
                            if (!mReceivedMessageIds.contains(msgFromDB.id)) {
                                Log.debug(TAG + "Message " + msgFromDB.id + " is expired, update expired");
                                msgFromDB.expired = true;
                                msgFromDB.label = Constants.Inbox.Messages.Label.EXPIRED;
                                Map<String, Object> msgValues = msgFromDB.toMap();
                                dataSnapshot.child(msgFromDB.id).getRef().updateChildren(msgValues);
                            }
                        }
                        readCategories();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        readCategories();
                    }
                });
            }
        }

        private void readCategories() {
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
