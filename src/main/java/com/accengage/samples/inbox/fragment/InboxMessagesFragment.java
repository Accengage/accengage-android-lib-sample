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
                case Constants.Inbox.Messages.PRIMARY:
                    name = ((InboxNavActivity) getActivity()).isArchived() ? getString(R.string.nav_inbox_archive) :
                            getString(R.string.nav_inbox_primary);
                    break;
                case Constants.Inbox.Messages.TRASH:
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
        boolean isArchived = ((InboxNavActivity) getActivity()).isArchived();
        String label = ((InboxNavActivity) getActivity()).getLabel();
        String category = ((InboxNavActivity) getActivity()).getCategory();

        Query recentPostsQuery = databaseReference.child(Constants.USER_INBOX_MESSAGES).child(mCurrentUser.getUid()).child(label)
                .limitToFirst(100);

        if (category == null) {
            // Primary / Archive / Trash
            if (label.equals(Constants.Inbox.Messages.PRIMARY)) {
                recentPostsQuery = recentPostsQuery.orderByChild("archived").equalTo(isArchived);
            }
        } else {
            // Category
            recentPostsQuery = recentPostsQuery.orderByChild("category").equalTo(category);
        }


        return recentPostsQuery;
    }
}
