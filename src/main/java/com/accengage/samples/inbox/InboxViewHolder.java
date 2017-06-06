package com.accengage.samples.inbox;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.firebase.models.InboxMessage;

public class InboxViewHolder extends RecyclerView.ViewHolder {

    private TextView mSender;
    private TextView mTitle;
    private TextView mBody;

    public InboxViewHolder(View itemView) {
        super(itemView);
        mSender = itemView.findViewById(R.id.inbox_msg_sender);
        mTitle = itemView.findViewById(R.id.inbox_msg_title);
        mBody = itemView.findViewById(R.id.inbox_msg_body);
    }

    public void bindToMessage(InboxMessage inboxMessage) {
        mSender.setText(inboxMessage.sender);
        mTitle.setText(inboxMessage.title);
        mBody.setText(inboxMessage.text);
    }
}
