package com.accengage.samples.inbox.fragment;


import android.content.Context;

import com.accengage.samples.R;
import com.accengage.samples.firebase.Constants;
import com.accengage.samples.inbox.InboxNavActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public class InboxMessagesFragment extends InboxListFragment {

    @Override
    public String getViewName(Context context) {
        String name;
        String label = ((InboxNavActivity) getActivity()).getLabel();
        String category = ((InboxNavActivity) getActivity()).getCategory();

        if (category == null) {
            switch (label) {
                case Constants.Inbox.Messages.Label.PRIMARY:
                    name = getString(R.string.nav_inbox_primary);
                    break;
                case Constants.Inbox.Messages.Label.ARCHIVE:
                    name = getString(R.string.nav_inbox_archive);
                    break;
                case Constants.Inbox.Messages.Label.EXPIRED:
                    name = getString(R.string.nav_inbox_expired);
                    break;
                case Constants.Inbox.Messages.Label.TRASH:
                    name = getString(R.string.nav_inbox_trash);
                    break;
                default:
                    name = getString(R.string.nav_inbox_primary);
                    break;
            }
        } else {
            name = category;
        }

        return name;
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        String label = ((InboxNavActivity) getActivity()).getLabel();
        String category = ((InboxNavActivity) getActivity()).getCategory();

        Query recentPostsQuery;
        if (category == null) {
            if (label.equals(Constants.Inbox.Messages.Label.TRASH)) {
                recentPostsQuery = databaseReference.child(Constants.USER_INBOX_MESSAGES).child(mCurrentUser.getUid()).child(Constants.Inbox.Messages.Box.TRASH);
            } else {
                recentPostsQuery = databaseReference.child(Constants.USER_INBOX_MESSAGES).child(mCurrentUser.getUid()).child(Constants.Inbox.Messages.Box.INBOX);
                // Filter by Label (Primary / Archive / Expired)
                recentPostsQuery = recentPostsQuery.orderByChild("label").equalTo(label);
            }
        } else {
            // Filter by Category
            recentPostsQuery = databaseReference.child(Constants.USER_INBOX_MESSAGES).child(mCurrentUser.getUid()).child(Constants.Inbox.Messages.Box.INBOX)
                                    .orderByChild("category").equalTo(category);
        }
        recentPostsQuery = recentPostsQuery.limitToFirst(100);

        return recentPostsQuery;
    }
}
