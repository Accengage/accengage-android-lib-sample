package com.accengage.samples.inbox;

import android.content.Context;

import com.ad4screen.sdk.Acc;
import com.ad4screen.sdk.Inbox;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;

import java.util.ArrayList;
import java.util.List;
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

    private InboxMessagesManager(Context context) {
        mContext = context;
        mDisposables = new CompositeDisposable();
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

    private class InboxCallable implements Callable<ObservableSource<? extends List<Message>>> {

        private List<Message> mMessages = null;
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
                            mMessages = new ArrayList<>(mCounter);
                            for (int i = 0; i < mInbox.countMessages(); i++) {
                                mInbox.getMessage(i, new Acc.MessageCallback() {

                                    @Override
                                    public void onResult(com.ad4screen.sdk.Message msg, final int index) {
                                        mMessages.add(msg);
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

            Log.debug("Notifying about " +  ((null != mMessages) ? mMessages.size() : "0") + " messages");
            return Observable.fromArray(mMessages);
        }
    };
}
