package com.accengage.samples.auth;


import com.accengage.samples.firebase.Constants;
import com.accengage.samples.firebase.models.User;
import com.ad4screen.sdk.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthUtils {

    private static final String TAG = "AuthUtils ";

    public static void writeUser(final FirebaseUser fabUser) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        final String userId = fabUser.getUid();
        db.child(Constants.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    Log.debug(TAG + "A new user, add it to DB");
                    user = new User(fabUser.getDisplayName(), fabUser.getEmail());
                    dataSnapshot.getRef().setValue(user);
                } else {
                    Log.debug(TAG + "DB user " + user.username + ", email: " + user.email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.warn(TAG + "writeUser onCancelled: " + databaseError.toString());
            }
        });
    }
}
