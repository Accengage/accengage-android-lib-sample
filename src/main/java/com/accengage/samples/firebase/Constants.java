package com.accengage.samples.firebase;

public abstract class Constants {

    public static final String USERS = "users";
    public static final String USER_INBOX_MESSAGES = "user-inbox-messages";
    public static final String USER_INBOX_CATEGORIES = "user-inbox-categories";

    public interface Inbox {
        interface Messages {

            interface Box {
                String INBOX = "inbox";
                String TRASH = "trash";
            }

            interface Label {
                String PRIMARY = "primary";
                String ARCHIVE = "archive";
                String EXPIRED = "expired";
                String TRASH = "trash";
            }
        }
    }

}
