package com.accengage.samples.inbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.accengage.samples.R;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.InboxMessageActivity;
import com.accengage.samples.inbox.InboxMessagesManager;
import com.accengage.samples.inbox.InboxViewHolder;
import com.ad4screen.sdk.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

public abstract class InboxListFragment extends AccengageFragment {

    private static final String TAG = "InboxListFragment";

    private DatabaseReference mDatabase;
    protected FirebaseUser mCurrentUser;

    private FirebaseRecyclerAdapter<InboxMessage, InboxViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inbox_messages;
    }

    @Override
    public void onCreatingView(View fragmentView) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRecycler = fragmentView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query postsQuery = getQuery(mDatabase);

        mAdapter = new FirebaseRecyclerAdapter<InboxMessage, InboxViewHolder>(InboxMessage.class, R.layout.item_inbox_message, InboxViewHolder.class, postsQuery) {

            @Override
            protected void populateViewHolder(InboxViewHolder viewHolder, InboxMessage message, int position) {
                Log.d(TAG, "populate message " + message.id);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InboxViewHolder holder = (InboxViewHolder) view.getTag();
                        InboxMessage inboxMessage = holder.getMessage();
                        Log.d(TAG, "click message " + inboxMessage.id);

                        Intent intent = new Intent(getActivity(), InboxMessageActivity.class);
                        intent.putExtra(InboxMessageActivity.EXTRA_MSG_KEY, inboxMessage.id);
                        startActivity(intent);
                        onClickMessage(inboxMessage);
                    }
                });
                viewHolder.bindToMessage(message);
            }

            private void onClickMessage(InboxMessage message) {
                if (!message.read) {

                    // Update Firebase DB
                    message.read = true;
                    Map<String, Object> msgValues = message.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    String path = "/user-inbxmessages/" + mCurrentUser.getUid() + "/" + message.id;
                    childUpdates.put(path, msgValues);
                    mDatabase.updateChildren(childUpdates);

                    // Update Accengage inbox
                    InboxMessagesManager manager = InboxMessagesManager.get(getActivity());
                    Message accengageMessage = manager.getMessage(message.id);
                    accengageMessage.setRead(true);
                    accengageMessage.hasBeenOpenedByUser(getActivity());
                    manager.updateMessages();
                }
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
