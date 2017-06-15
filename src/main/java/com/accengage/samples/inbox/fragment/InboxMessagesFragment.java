package com.accengage.samples.inbox.fragment;


import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public class InboxMessagesFragment extends InboxListFragment {

    @Override
    public String getViewName(Context context) {
        return "Inbox Messages";
    }


    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys

        Query recentPostsQuery = databaseReference.child("user-inbxmessages").child(mCurrentUser.getUid())
                .limitToFirst(100);

        return recentPostsQuery;
    }
}
