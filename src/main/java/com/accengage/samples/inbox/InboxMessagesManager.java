package com.accengage.samples.inbox;

import android.content.Context;

import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.A4S;
import com.ad4screen.sdk.Inbox;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class InboxMessagesManager {

    private static InboxMessagesManager instance;

    private Inbox mInbox;
    private Context mContext;
    private CompositeDisposable mDisposables;
    private Map<String, Message> mMessageMap;

    private DatabaseReference mDatabase;

    private InboxMessagesManager(Context context) {
        mContext = context;
        mDisposables = new CompositeDisposable();
        mMessageMap = new HashMap<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static InboxMessagesManager get(Context context) {
        if (instance == null) {
            synchronized (InboxMessagesManager.class) {
                if (instance == null) {
                    instance = new InboxMessagesManager(context);
                }
            }
        }
        return instance;
    }

    public void subscribeForMessages(DisposableObserver<Message> observer) {
        Observable.create(new InboxObservable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer);
        mDisposables.add(observer);
    }

    public void unsubscribeFromMessages(DisposableObserver<Message> observer) {
        mDisposables.remove(observer);
    }

    public Message getMessage(String id) {
        return mMessageMap.get(id);
    }

    public void updateMessage(InboxMessage inboxMessage) {
        // Update Firebase DB
        Map<String, Object> msgValues = inboxMessage.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String path = inboxMessage.getPath();
        childUpdates.put(path, msgValues);
        mDatabase.updateChildren(childUpdates);

        // Update Accengage inbox
        Message accMessage = getMessage(inboxMessage.id);
        if (accMessage != null) {
            if (inboxMessage.isUpdateRequiredForAccMessage(accMessage)) {
                A4S.get(mContext).updateMessages(mInbox);
            }
        } else {
            Log.warn("There is no accengage message '" + inboxMessage.id + "' check a connection with Accengage server");
        }
    }

    private class InboxObservable implements ObservableOnSubscribe<Message> {

        private int mCounter = -1;
        private boolean mIsWaiting = false;
        private Object mLock = new Object();
        private ObservableEmitter<Message> mSubscriber;

        @Override
        public void subscribe(ObservableEmitter<Message> subscriber) throws Exception {

            mSubscriber = subscriber;
            A4S.get(mContext).getInbox(new A4S.Callback<Inbox>() {
                @Override
                public void onResult(Inbox result) {

                    synchronized (mLock) {
                        mInbox = result;
                        if (mInbox != null) {
                            mCounter = mInbox.countMessages();
                            if (mCounter == 0) {
                                Log.debug("There is no Inbox messages");
                                notifyIfWaiting();
                                return;
                            }

                            Log.debug("There is(are) " + mCounter + " Inbox message(s)");
                            for (int i = 0; i < mInbox.countMessages(); i++) {
                                mInbox.getMessage(i, new A4S.MessageCallback() {

                                    @Override
                                    public void onResult(com.ad4screen.sdk.Message msg, final int index) {
                                        Log.debug("onResult message id: " + msg.getId() + ", title: " + msg.getTitle());
                                        mMessageMap.put(msg.getId(), msg);
                                        mSubscriber.onNext(msg);
                                        if (--mCounter == 0) {
                                            notifyIfWaiting();
                                        }
                                    }

                                    @Override
                                    public void onError(int error, String errorMessage) {
                                        if (--mCounter == 0) {
                                            notifyIfWaiting();
                                        }
                                    }
                                });
                            }
                        } else {
                            mCounter = 0;
                            Log.warn("There is a null Inbox");
                            notifyIfWaiting();
                        }
                    }
                }

                @Override
                public void onError(int error, String errorMessage) {
                    synchronized (mLock) {
                        mCounter = 0;
                        Log.error("There is an error while getting Inbox messages");
                        notifyIfWaiting();
                    }
                }

                private void notifyIfWaiting() {
                    synchronized (mLock) {
                        Log.debug("Notify waiting thread for inbox messages");
                        if (mIsWaiting) {
                            mIsWaiting = false;
                            mLock.notify();
                        }
                    }
                }

            });

            // wait for the result
            synchronized (mLock) {
                while (mCounter < 0) {
                    try {
                        Log.debug("Waiting for Inbox messages...");
                        mIsWaiting = true;
                        mLock.wait();
                    } catch(InterruptedException e) {
                        Log.error("An error is occured while getting Inbox messages: " + e);
                        mSubscriber.onError(new Exception());
                    }
                }
            }

            Log.debug(mMessageMap.size() + " messages are received");
            mSubscriber.onComplete();
        }
    }
}
