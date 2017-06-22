package com.accengage.samples.firebase.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class InboxCategory {

    public String name;
    public int messageCount;

    public InboxCategory() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public InboxCategory(String name, int messageCount) {
        this.name = name;
        this.messageCount = messageCount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("messageCount", messageCount);
        return result;
    }
}
