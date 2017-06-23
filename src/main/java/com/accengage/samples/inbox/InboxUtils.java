package com.accengage.samples.inbox;


import android.content.Context;

import com.accengage.samples.firebase.Constants;
import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class InboxUtils {

    public static final String TAG = "InboxUtils ";

    public static void archiveMessage(Context context, InboxMessage message) {
        message.archived = true;
        InboxMessagesManager.get(context).updateMessage(message);
    }

    public static void moveMessageTo(final String label, final InboxMessage message) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(label).
                child(message.id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InboxMessage msgFromDB = dataSnapshot.getValue(InboxMessage.class);
                String previousLabel = message.label;
                message.label = label;
                if (msgFromDB == null) {
                    Log.debug(TAG + "Add message " + message.id + " to " + label);
                    dataSnapshot.getRef().setValue(message);
                } else {
                    Log.warn(TAG + "Inbox message " + msgFromDB.id + " is already existed in the trash");
                    // check if instances are equal
                    if (!message.equals(msgFromDB)) {
                        Log.debug(TAG + "Update Inbox message " + msgFromDB.id + " in trash");
                        Map<String, Object> msgValues = message.toMap();
                        dataSnapshot.getRef().updateChildren(msgValues);
                    }
                }
                // Remove the message from the previous location (label)
                dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(previousLabel)
                        .child(message.id).removeValue();
                // Remove the message id from corresponding category
                // TODO That's the question if we should remove a message from it's category if the message is in trash/expired
                // TODO it would be better to do not remove the message from the category but display a trash/expired label on the item view
                /*if (!TextUtils.isEmpty(mMessage.category)) {
                    dbRef.child(Constants.USER_INBOX_CATEGORIES).child(mMessage.uid).child(mMessage.category)
                            .child(mMessage.id).removeValue();
                }*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.error(TAG + "onCancelled moveMessageTo " + label + ", error: " + databaseError);
            }
        });
    }
}
