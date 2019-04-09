package com.accengage.samples.inbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Message;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class InboxViewHolder extends RecyclerView.ViewHolder {

    private TextView mSender;
    private TextView mTitle;
    private TextView mShortText;
    private ImageView mIconView;
    private TextView mSentTime;

    private InboxMessage mMessage;

    public InboxViewHolder(View itemView) {
        super(itemView);

        itemView.setTag(this);
        mSender = itemView.findViewById(R.id.inbox_msg_sender);
        mTitle = itemView.findViewById(R.id.inbox_msg_title);
        mShortText = itemView.findViewById(R.id.inbox_msg_short_text);
        mIconView = itemView.findViewById(R.id.inbox_msg_sender_photo);
        mSentTime = itemView.findViewById(R.id.inbox_msg_sent_time);
    }

    public void bindToMessage(Context context, InboxMessage inboxMessage) {
        mMessage = inboxMessage;

        if (!inboxMessage.read) {
            mSender.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mTitle.setTextColor(Color.BLACK);
            mSentTime.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mSentTime.setTextColor(context.getResources().getColor(R.color.accent));
        }

        if (!TextUtils.isEmpty(mMessage.icon)) {
            loadAndSetIcon(context, mIconView, mMessage.icon);
        }
        mSender.setText(inboxMessage.sender);
        mTitle.setText(inboxMessage.title);
        if (!inboxMessage.text.isEmpty()) {
            mShortText.setText(inboxMessage.text);
        } else {
             if (inboxMessage.contentType.equals(Message.MessageContentType.Text.name()) &&
                !inboxMessage.body.isEmpty()) {
                 mShortText.setText(inboxMessage.body);
             }
        }
        if (DateUtils.isToday(mMessage.getSentTime())) {
            mSentTime.setText(mMessage.getFormatedTime());
        } else {
            mSentTime.setText(mMessage.getFormatedDate());
        }
    }

    public static void loadAndSetIcon(final Context context, final ImageView imageView, String url) {
        Glide.with(context).load(url).asBitmap().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public InboxMessage getMessage() {
        return mMessage;
    }
}
