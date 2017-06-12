package com.accengage.samples.inbox;

import android.content.Context;

import com.accengage.samples.firebase.models.InboxMessage;
import com.ad4screen.sdk.Acc;
import com.ad4screen.sdk.Inbox;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
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
    private FirebaseUser mCurrentUser;

    private InboxMessagesManager(Context context) {
        mContext = context;
        mDisposables = new CompositeDisposable();
        mMessageMap = new HashMap<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
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

    public void subscribeForMessages(DisposableObserver<List<Message>> observer) {
        Observable<List<Message>> observable = Observable
                .defer(new InboxCallable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        observable.subscribeWith(observer);
        mDisposables.add(observer);
    }

    public void unsubscribeFromMessages(DisposableObserver<List<Message>> observer) {
        mDisposables.remove(observer);
    }

    public Message getMessage(String id) {
        return mMessageMap.get(id);
    }

    public void updateMessage(InboxMessage inboxMessage) {
        // Update Firebase DB
        Map<String, Object> msgValues = inboxMessage.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        String path = "/user-inbxmessages/" + mCurrentUser.getUid() + "/" + inboxMessage.id;
        childUpdates.put(path, msgValues);
        mDatabase.updateChildren(childUpdates);

        // Update Accengage inbox
        Message accMessage = getMessage(inboxMessage.id);
        if (inboxMessage.updateAccMessage(accMessage)) {
            Acc.get(mContext).updateMessages(mInbox);
        }
    }

    private class InboxCallable implements Callable<ObservableSource<? extends List<Message>>> {

        private int mCounter = -1;
        private boolean mIsWaiting = false;
        private Object mLock = new Object();

        @Override
        public ObservableSource<? extends List<Message>> call() throws Exception {

            Acc.get(mContext).getInbox(new Acc.Callback<Inbox>() {
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
                                mInbox.getMessage(i, new Acc.MessageCallback() {

                                    @Override
                                    public void onResult(com.ad4screen.sdk.Message msg, final int index) {
                                        try {
                                            String id = (String) InboxMessage.getProperty(msg, "id");
                                            Log.debug("onResult message id: " + id + ", title: " + msg.getTitle());
                                            mMessageMap.put(id, msg);
                                        } catch (NoSuchFieldException e) {
                                            e.printStackTrace();
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
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
                    }
                }
            }

            Log.debug("Notifying about " +  mMessageMap.size() + " messages");
            return Observable.fromArray(new ArrayList<>(mMessageMap.values()));
        }
    }

}
