package com.accengage.samples.firebase.models;

import android.os.Parcel;

import com.ad4screen.sdk.Message;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class InboxButton {

    @Exclude
    public int position;

    public String id;
    public String title;
    public HashMap<String, String> params;
    public String messageId;

    public InboxButton() {
        // Default constructor required for calls to DataSnapshot.getValue(InboxButton.class)
    }

    public InboxButton(String messageId, Message.Button button) {
        this.id = button.getId();
        this.title = button.getTitle();
        this.params = button.getCustomParameters();
        this.messageId = messageId;
    }

    protected InboxButton(Parcel in) {
        position = in.readInt();
        id = in.readString();
        title = in.readString();
        messageId = in.readString();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("params", params);
        result.put("messageId", messageId);
        return result;
    }

    @Exclude
    public Message.Button getAccButton() {
        Parcel parcel = Parcel.obtain();

        parcel.writeString(messageId);
        parcel.writeString(id);
        parcel.writeString(title);
        if (params != null) {
            parcel.writeInt(params.size());
            for (String s : params.keySet()) {
                parcel.writeString(s);
                parcel.writeString(params.get(s));
            }
        } else {
            parcel.writeInt(0);
        }

        parcel.setDataPosition(0);
        return Message.Button.CREATOR.createFromParcel(parcel);
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InboxButton) {
            InboxButton btn = (InboxButton) obj;
            // TODO check params
            if (id.equals(btn.id) && title.equals(btn.title) && messageId.equals(btn.messageId)) {
                return true;
            }
        }
        return false;
    }
}
