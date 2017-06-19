package com.accengage.samples.inbox;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Message;
import com.squareup.picasso.Picasso;

public class InboxViewHolder extends RecyclerView.ViewHolder {

    private TextView mSender;
    private TextView mTitle;
    private TextView mShortText;
    private ImageView mIconView;

    private InboxMessage mMessage;

    public InboxViewHolder(View itemView) {
        super(itemView);

        itemView.setTag(this);
        mSender = itemView.findViewById(R.id.inbox_msg_sender);
        mTitle = itemView.findViewById(R.id.inbox_msg_title);
        mShortText = itemView.findViewById(R.id.inbox_msg_short_text);
        mIconView = itemView.findViewById(R.id.inbox_msg_sender_photo);
    }

    public void bindToMessage(Context context, InboxMessage inboxMessage) {
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

        if (TextUtils.isEmpty(mMessage.icon)) {
            mIconView.setImageResource(0);
        } else {
            loadAndSetIcon(context, mIconView, mMessage.icon);
        }
    }

    public static void loadAndSetIcon(Context context, final ImageView imageView, String path) {
        Picasso.Builder picassoBuilder = new Picasso.Builder(context);
        picassoBuilder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                imageView.setImageResource(0);
            }
        });
        picassoBuilder.build().load(path).into(imageView);
    }

    public InboxMessage getMessage() {
        return mMessage;
    }
}
