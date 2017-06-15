package com.accengage.samples.inbox.fragment;


import android.content.Context;

import com.accengage.samples.R;
import com.accengage.samples.inbox.InboxNavActivity;
import com.ad4screen.sdk.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public class InboxMessagesFragment extends InboxListFragment {

    @Override
    public String getViewName(Context context) {
        String name = ((InboxNavActivity) getActivity()).isArchived() ? getString(R.string.nav_inbox_archive) :
                getString(R.string.nav_inbox_primary);
        return name;
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys

        boolean isArchived = ((InboxNavActivity) getActivity()).isArchived();
        Log.debug("andrei getQuery " + isArchived);
        Query recentPostsQuery = databaseReference.child("user-inbxmessages").child(mCurrentUser.getUid())
                .limitToFirst(100).orderByChild("archived").equalTo(isArchived);

        return recentPostsQuery;
    }
}
