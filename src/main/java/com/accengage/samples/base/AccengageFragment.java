package com.accengage.samples.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.accengage.samples.tracking.FirebaseTracker;
import com.accengage.samples.tracking.Tracker;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class AccengageFragment extends Fragment {

    protected Tracker mTracker;

    private Unbinder mFragmentUnbinder;
    private OnFragmentCreateViewListener mListener;

    public interface OnFragmentCreateViewListener {
        void onFragmentCreateViewDone(AccengageFragment fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                mListener = (OnFragmentCreateViewListener) activity;
            }
            catch (ClassCastException e) {}
        }
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(getLayoutResId(), container, false);
        mFragmentUnbinder = ButterKnife.bind(this, fragmentView);
        mTracker = getTracker();
        onCreatingView(fragmentView);
        return fragmentView;
    }

    protected Tracker getTracker() {
        return new FirebaseTracker(this.getContext());
    }

    public void onCreatingView(View fragmentView) {
        if (mListener != null) {
            mListener.onFragmentCreateViewDone(this);
        }
    }

    public abstract String getViewName(Context context);

    protected abstract int getLayoutResId();

    protected void displayMessageToUser(String message) {
        displayMessageToUser(message, null);
    }

    protected void displayMessageToUser(String message, View viewToFocus) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        if (viewToFocus != null) {
            viewToFocus.requestFocus();
            if (viewToFocus instanceof EditText) {
                ((EditText) viewToFocus).setError(message);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFragmentUnbinder.unbind();
    }
}
