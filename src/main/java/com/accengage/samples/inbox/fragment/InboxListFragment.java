package com.accengage.samples.inbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.accengage.samples.R;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.InboxMessageActivity;
import com.accengage.samples.inbox.InboxViewHolder;
import com.ad4screen.sdk.Log;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public abstract class InboxListFragment extends AccengageFragment {

    private DatabaseReference mDatabase;

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

        mRecycler = (RecyclerView) fragmentView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
        Log.debug("andrei onCreatingView");
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

        //new FirebaseArray(postsQuery);
        mAdapter = new FirebaseRecyclerAdapter<InboxMessage, InboxViewHolder>(InboxMessage.class, R.layout.item_inbox_message, InboxViewHolder.class, postsQuery) {

            @Override
            protected void populateViewHolder(InboxViewHolder viewHolder, InboxMessage model, int position) {
                Log.debug("andrei populate message " + model.id);
                DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), InboxMessageActivity.class);
                        intent.putExtra(InboxMessageActivity.EXTRA_MSG_KEY, postKey);
                        startActivity(intent);
                    }
                });
                viewHolder.bindToMessage(model);
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
