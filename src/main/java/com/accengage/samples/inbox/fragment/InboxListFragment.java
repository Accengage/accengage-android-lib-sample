package com.accengage.samples.inbox.fragment;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.accengage.samples.R;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.InboxMessagesManager;
import com.accengage.samples.inbox.InboxNavActivity;
import com.accengage.samples.inbox.InboxViewHolder;
import com.accengage.samples.tracking.Tracker;
import com.ad4screen.sdk.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public abstract class InboxListFragment extends AccengageFragment {

    private static final String TAG = "InboxListFragment";

    private DatabaseReference mDatabase;
    protected FirebaseUser mCurrentUser;

    private FirebaseRecyclerAdapter<InboxMessage, InboxViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private InboxMessagesManager mInboxManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inbox_messages;
    }

    @Override
    public void onCreatingView(View fragmentView) {
        super.onCreatingView(fragmentView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mInboxManager = InboxMessagesManager.get(getActivity());

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

        FirebaseRecyclerOptions<InboxMessage> options =
                new FirebaseRecyclerOptions.Builder<InboxMessage>()
                .setQuery(postsQuery, InboxMessage.class)
                .setLifecycleOwner(this)
                .build();
        mAdapter = new FirebaseRecyclerAdapter<InboxMessage, InboxViewHolder>(options) {

            @Override
            public InboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new InboxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox_message, parent, false));
            }

            @Override
            protected void onBindViewHolder(InboxViewHolder holder, int position, InboxMessage message) {
                populateViewHolder(holder, message);
            }

            private void populateViewHolder(InboxViewHolder viewHolder, InboxMessage message) {
                Log.d(TAG, "populate message " + message.id);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InboxViewHolder holder = (InboxViewHolder) view.getTag();
                        InboxMessage inboxMessage = holder.getMessage();
                        Log.d(TAG, "click message " + inboxMessage.id);
                        onClickMessage(inboxMessage);
                        ((InboxNavActivity) getActivity()).setClickedMessage(inboxMessage);
                        ((InboxNavActivity) getActivity()).displayFragment(InboxMessageDetailFragment.class);
                    }
                });
                viewHolder.bindToMessage(InboxListFragment.this.getContext(), message);
                onDisplayMessage(message);
            }

            private void onDisplayMessage(InboxMessage message) {
                if (!message.displayed) { // TODO displayed is always to false afer getting messages from Accengage
                    message.displayed = true;
                    mInboxManager.updateMessage(message);

                    Tracker tracker = getTracker();
                    tracker.trackMessageDisplay(message.id); // Implemented only for Firebase

                    // TODO Tracking should be done in AccengageTracker, lines below must be removed
                    Message accMessage = mInboxManager.getMessage(message.id);
                    if (accMessage != null) {
                        accMessage.hasBeenDisplayedToUser(getActivity());
                    } else {
                        Log.w(TAG, "There is no accengage message '" + message.id + "' check a connection with Accengage server");
                    }
                }
            }

            private void onClickMessage(InboxMessage message) {
                if (!message.read) {
                    message.read = true;
                    mInboxManager.updateMessage(message);
                }

                Tracker tracker = getTracker();
                tracker.trackMessageClick(message.id); // Implemented only for Firebase

                // TODO Tracking should be done in AccengageTracker, lines below must be removed
                Message accMessage = mInboxManager.getMessage(message.id);
                if (accMessage != null) {
                    accMessage.hasBeenOpenedByUser(getActivity());
                } else {
                    Log.w(TAG, "There is no accengage message '" + message.id + "' check a connection with Accengage server");
                }
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
