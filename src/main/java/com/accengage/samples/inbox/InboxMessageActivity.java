package com.accengage.samples.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.base.BaseActivity;
import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class InboxMessageActivity extends BaseActivity {

    public static final String TAG = "InboxMessageActivity";
    public static final String EXTRA_MSG_KEY = "message_key";

    private String mMessageKey;

    private DatabaseReference mMessageReference;
    private ValueEventListener mMessageListener;

    private TextView mSender;
    private TextView mTitle;
    private TextView mBody;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message);

        mMessageKey = getIntent().getStringExtra(EXTRA_MSG_KEY);
        if (mMessageKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_MSG_KEY");
        }

        Log.d(TAG, "onCreate message_key " + mMessageKey);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mMessageReference = FirebaseDatabase.getInstance().getReference()
                .child("user-inbxmessages").child(user.getUid()).child(mMessageKey);

        mSender = findViewById(R.id.inbox_msg_sender);
        mTitle = findViewById(R.id.inbox_msg_title);
        mBody = findViewById(R.id.inbox_msg_body);
        mWebView = findViewById(R.id.inbox_msg_webview);
    }

    @Override
    public void onStart() {
        super.onStart();

        mMessageListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InboxMessage msgFromDB = dataSnapshot.getValue(InboxMessage.class);
                mSender.setText(msgFromDB.sender);
                mTitle.setText(msgFromDB.title);

                if (msgFromDB.contentType.equals(Message.MessageContentType.Web.name())) {
                    Log.d(TAG, "message with a web content");
                    if (msgFromDB.body != null) {
                        Log.d(TAG, "message with a web body is not null " + msgFromDB.body);
                        mWebView.setVisibility(View.VISIBLE);
                        mWebView.setWebViewClient(new WebViewClient());
                        WebSettings webSettings = mWebView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        mWebView.loadUrl(msgFromDB.body);
                        mBody.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "message with an other content: " + msgFromDB.contentType);
                    mWebView.setVisibility(View.GONE);
                    mBody.setText(msgFromDB.body);
                    mBody.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessageReference.addValueEventListener(mMessageListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mMessageListener != null) {
            mMessageReference.removeEventListener(mMessageListener);
            mMessageReference = null;
        }
    }
}
