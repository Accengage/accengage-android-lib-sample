package com.accengage.samples.inbox;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Message;

public class InboxViewHolder extends RecyclerView.ViewHolder {

    private TextView mSender;
    private TextView mTitle;
    private TextView mShortText;

    private InboxMessage mMessage;

    public InboxViewHolder(View itemView) {
        super(itemView);

        itemView.setTag(this);
        mSender = itemView.findViewById(R.id.inbox_msg_sender);
        mTitle = itemView.findViewById(R.id.inbox_msg_title);
        mShortText = itemView.findViewById(R.id.inbox_msg_short_text);
    }

    public void bindToMessage(InboxMessage inboxMessage) {
        mMessage = inboxMessage;
        mSender.setText(inboxMessage.sender);

        mTitle.setText(inboxMessage.title);
        if (inboxMessage.read) {
            mTitle.setTypeface(Typeface.DEFAULT);
        }

        if (!inboxMessage.text.isEmpty()) {
            mShortText.setText(inboxMessage.text);
        } else {
             if (inboxMessage.contentType.equals(Message.MessageContentType.Text.name()) &&
                !inboxMessage.body.isEmpty()) {
                 mShortText.setText(inboxMessage.body);
             }
        }
    }

    public InboxMessage getMessage() {
        return mMessage;
    }
}
