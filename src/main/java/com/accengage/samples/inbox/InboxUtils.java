package com.accengage.samples.inbox;


import android.content.Context;
import android.text.TextUtils;

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
        message.label = Constants.Inbox.Messages.Label.ARCHIVE;
        InboxMessagesManager.get(context).updateMessage(message);
    }

    public static void moveMessageTo(final String box, final InboxMessage message) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(box).
                child(message.id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InboxMessage msgFromDB = dataSnapshot.getValue(InboxMessage.class);
                message.label = box;
                if (msgFromDB == null) {
                    Log.debug(TAG + "Add message " + message.id + " to " + box);
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
                dbRef.child(Constants.USER_INBOX_MESSAGES).child(message.uid).child(Constants.Inbox.Messages.Box.INBOX)
                        .child(message.id).removeValue();

                // We should remove a message from it's category if the message is moved to trash
                // to not have it in the output result filtered by the corresponding category
                if (box.equals(Constants.Inbox.Messages.Box.TRASH)) {
                    if (!TextUtils.isEmpty(message.category)) {
                        dbRef.child(Constants.USER_INBOX_CATEGORIES).child(message.uid).child(message.category)
                                .child(message.id).removeValue();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.error(TAG + "onCancelled moveMessageTo " + box + ", error: " + databaseError);
            }
        });
    }
}
