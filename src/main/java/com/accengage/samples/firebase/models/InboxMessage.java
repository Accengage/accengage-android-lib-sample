package com.accengage.samples.firebase.models;

import android.os.Parcel;

import com.accengage.samples.firebase.Constants;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@IgnoreExtraProperties
public class InboxMessage {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Accengage inbox members
    public String id;
    public String title;
    public String contentType;
    public String sendDate;
    public String body;
    public String sender;
    public String category;
    public String text;
    public boolean expired;
    public boolean read;
    public boolean archived;
    public boolean displayed;
    public boolean downloaded;
    public String icon;
    public int buttonCount;
    public List<InboxButton> buttons;

    public String uid;
    public String label;

    public InboxMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(InboxMessage.class)
        buttons = new ArrayList<>(0);
    }

    public InboxMessage(Message message, String uid) {

        this.id = message.getId();
        this.title = message.getTitle();
        this.sendDate = convertDateToString(message.getSendDate());
        this.body = message.getBody();
        this.sender = message.getSender();
        this.category = message.getCategory();
        this.text = message.getText();
        this.expired = message.isOutdated();
        this.read = message.isRead();
        this.archived = message.isArchived();
        this.displayed = message.isDisplayed();
        this.downloaded = message.isDownloaded();
        this.icon = message.getUrlIcon();
        this.contentType = message.getContentType().name();
        this.buttonCount = message.countButtons();
        this.buttons = new ArrayList<>(buttonCount);
        for (int i = 0; i < buttonCount; i++) {
            Message.Button button = message.getButton(i);
            buttons.add(new InboxButton(id, button));
        }

        this.uid = uid;
        this.label = Constants.Inbox.Messages.Label.PRIMARY;
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
        result.put("expired", expired);
        result.put("read", read);
        result.put("archived", archived);
        result.put("displayed", displayed);
        result.put("downloaded", downloaded);
        result.put("icon", icon);
        result.put("uid", uid);
        result.put("label", label);
        result.put("buttonCount", buttonCount);
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
                expired == msg.expired &&
                read == msg.read &&
                archived == msg.archived &&
                displayed == msg.displayed &&
                downloaded == msg.downloaded &&
                icon.equals(msg.icon) &&
                uid.equals(msg.uid) &&
                label.equals(msg.label)
                ) {

            // Check buttons
            if (buttonCount == msg.buttonCount && buttons.equals(msg.buttons)) {
                return true;
            }

            return false;
        }
        return false;
    }

    @Exclude
    public Message getAccMessage() {

        Parcel parcel = Parcel.obtain();
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeLong(convertStringToDate(sendDate).getTime());
        parcel.writeString(body);
        parcel.writeString(sender);
        parcel.writeString(category);
        parcel.writeString(text);
        parcel.writeString(contentType);
        parcel.writeString(icon);
        boolean[] boolArray = {expired, displayed, read, archived, downloaded};
        parcel.writeBooleanArray(boolArray);
        parcel.writeArray(getAccButtons());
        // TODO
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

    @Exclude
    private Message.Button[] getAccButtons() {
        if (buttonCount == 0)
            return null;

        int i = 0;
        Message.Button[] accButtons = new Message.Button[buttonCount];
        for (InboxButton button : buttons) {
            accButtons[i] = button.getAccButton();
        }
        return accButtons;
    }

    @Exclude
    public boolean isUpdateRequiredForAccMessage(Message message) {
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

    @Exclude
    public String getPath() {
        String box = (label.equals(Constants.Inbox.Messages.Box.TRASH)) ?
                Constants.Inbox.Messages.Box.TRASH : Constants.Inbox.Messages.Box.INBOX;
        return "/" + Constants.USER_INBOX_MESSAGES + "/" + uid + "/" + box + "/" + id;
    }

    @Exclude
    public long getSentTime() {
        return convertStringToDate(sendDate).getTime();
    }

    @Exclude
    public String getFormatedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        return dateFormat.format(convertStringToDate(sendDate));
    }

    @Exclude
    public String getFormatedTime() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(convertStringToDate(sendDate));
    }

    @Exclude
    public String getFormatedDateTime() {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(convertStringToDate(sendDate));
    }

    @Exclude
    private static String convertDateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        return dateFormat.format(date);
    }

    @Exclude
    private static Date convertStringToDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            Log.error("ContentHelper|convertStringToDate exception: " + e.toString());
            return new Date(0);
        }
    }
}
