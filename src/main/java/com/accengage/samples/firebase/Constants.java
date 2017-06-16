package com.accengage.samples.firebase;

public abstract class Constants {

    public static final String USERS = "users";
    public static final String USER_INBOX_MESSAGES = "user-inbox-messages";

    public interface Inbox {
        interface Messages {
            String PRIMARY = "primary";
            String TRASH = "trash";
        }
    }

}
