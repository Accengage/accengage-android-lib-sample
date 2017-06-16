package com.accengage.samples.firebase.models;

import android.os.Parcel;

import com.accengage.samples.firebase.Constants;
import com.ad4screen.sdk.Message;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class InboxMessage {

    // Accengage inbox members
    public String id;
    public String title;
    public String contentType;
    public Date sendDate; // TODO convert to string
    public String body;
    //public String data;
    public String sender;
    public String category;
    //public String trackingURL;
    public String text;
    //public String type;
    public boolean outdated;
    public boolean read;
    public boolean archived;
    public boolean displayed;
    public boolean downloaded;
    //public boolean updated;
    public String icon;

    public String uid;
    public String label;

    public InboxMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public InboxMessage(Message message, String uid) {

        try {
            this.id = (String) getProperty(message, "id");
            this.title = message.getTitle();
            this.sendDate = message.getSendDate();
            this.body = message.getBody();
            //this.data = (String) getProperty(message, "data");
            this.sender = message.getSender();
            this.category = message.getCategory();
            //this.trackingURL = (String) getProperty(message, "trackingUrl");
            this.text = message.getText();
            //this.type = message.type.name();
            this.outdated = message.isOutdated();
            this.read = message.isRead();
            this.archived = message.isArchived();
            this.displayed = message.isDisplayed();
            this.downloaded = message.isDownloaded();
            //this.updated = (boolean) getProperty(message, "updated");
            this.icon = (String) getProperty(message, "icon");
            this.contentType = message.getContentType().name();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.uid = uid;
        this.label = Constants.Inbox.Messages.PRIMARY;
    }

    @Exclude
    public static Object getProperty(Message message, String propertyName) throws NoSuchFieldException, IllegalAccessException {
        Field f = Message.class.getDeclaredField(propertyName);
        f.setAccessible(true);
        return f.get(message);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("contentType", contentType);
        result.put("sendDate", sendDate);
        result.put("body", body);
        result.put("sender", sender);
        result.put("category", category);
        result.put("text", text);
        result.put("outdated", outdated);
        result.put("read", read);
        result.put("archived", archived);
        result.put("displayed", displayed);
        result.put("downloaded", downloaded);
        result.put("icon", icon);
        result.put("uid", uid);
        result.put("label", label);
        return result;
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof InboxMessage)) {
            return false;
        }
        InboxMessage msg = (InboxMessage) obj;
        if (id.equals(msg.id) &&
                title.equals(msg.title) &&
                contentType.equals(msg.contentType) &&
                sendDate.equals(msg.sendDate) &&
                body.equals(msg.body) &&
                sender.equals(msg.sender) &&
                category.equals(msg.category) &&
                text.equals(msg.text) &&
                outdated == msg.outdated &&
                read == msg.read &&
                archived == msg.archived &&
                displayed == msg.displayed &&
                downloaded == msg.downloaded &&
                icon.equals(msg.icon) &&
                uid.equals(msg.uid) &&
                label.equals(msg.label)
                ) {
            return true;
        }
        return false;
    }

    @Exclude
    public Message getAccMessage() {

        Parcel parcel = Parcel.obtain();
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeLong(sendDate.getTime());
        parcel.writeString(body);
        parcel.writeString(sender);
        parcel.writeString(category);
        parcel.writeString(text);
        parcel.writeString(contentType);
        parcel.writeString(icon);
        boolean[] boolArray = {outdated, displayed, read, archived, downloaded};
        parcel.writeBooleanArray(boolArray);
        // TODO
//        parcel.writeArray(buttons);
//        if (params != null) {
//            parcel.writeInt(params.size());
//            for (String s : params.keySet()) {
//                parcel.writeString(s);
//                parcel.writeString(params.get(s));
//            }
//        } else {
//            parcel.writeInt(0);
//        }

        parcel.setDataPosition(0);
        return Message.CREATOR.createFromParcel(parcel);
    }

    public boolean updateAccMessage(Message message) {
        boolean updated = false;
        if (message.isRead() != read) {
            updated = true;
            message.setRead(read);
        }
        if (message.isArchived() != archived) {
            updated = true;
            message.setArchived(archived);
        }
        if (message.isDisplayed() != displayed) {
            updated = true;
            message.setDisplayed(displayed);
        }
        return updated;
    }

    public String getPath() {
        return "/" + Constants.USER_INBOX_MESSAGES + "/" + uid + "/" + label + "/" + id;
    }
}
