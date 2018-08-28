package com.sensorsdata.analytics.android.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.lang.reflect.Field;

public class SensorsDataAppClickOverlayLayout extends FrameLayout {

    public SensorsDataAppClickOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    public SensorsDataAppClickOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SensorsDataAppClickOverlayLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("unused")
    private void init(Context context) {

    }

    @TargetApi(15)
    private boolean hasOnClickListeners(View view) {
        return view.hasOnClickListeners();
    }

    private boolean isContainView(View view, MotionEvent event) {
        double x = event.getRawX();
        double y = event.getRawY();
        Rect outRect = new Rect();
        view.getGlobalVisibleRect(outRect);
        return outRect.contains((int) x, (int) y);
    }

    @SuppressWarnings("all")
    private View getTargetView(ViewGroup viewGroup, MotionEvent event) {
        if (viewGroup == null) {
            return null;
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (!view.isShown()) {
                continue;
            }
            if (isContainView(view, event)) {
                if (hasOnClickListeners(view) || view instanceof CompoundButton ||
                        view instanceof SeekBar ||
                        view instanceof RatingBar) {
                    return view;
                } else if (view instanceof Spinner) {
                    AdapterView.OnItemSelectedListener onItemSelectedListener =
                            ((Spinner) view).getOnItemSelectedListener();
                    if (onItemSelectedListener != null &&
                            !(onItemSelectedListener instanceof WrapperAdapterViewOnItemSelectedListener)) {
                        ((Spinner) view).setOnItemSelectedListener(
                                new WrapperAdapterViewOnItemSelectedListener(onItemSelectedListener));
                    }
                } else if (view instanceof ExpandableListView) {
                    try {
                        Class viewClazz = Class.forName("android.widget.ExpandableListView");
                        //Child
                        Field mOnChildClickListenerField = viewClazz.getDeclaredField("mOnChildClickListener");
                        if (!mOnChildClickListenerField.isAccessible()) {
                            mOnChildClickListenerField.setAccessible(true);
                        }
                        ExpandableListView.OnChildClickListener onChildClickListener =
                                (ExpandableListView.OnChildClickListener) mOnChildClickListenerField.get(view);
                        if (onChildClickListener != null &&
                                !(onChildClickListener instanceof WrapperOnChildClickListener)) {
                            ((ExpandableListView) view).setOnChildClickListener(
                                    new WrapperOnChildClickListener(onChildClickListener));
                        }

                        //Group
                        Field mOnGroupClickListenerField = viewClazz.getDeclaredField("mOnGroupClickListener");
                        if (!mOnGroupClickListenerField.isAccessible()) {
                            mOnGroupClickListenerField.setAccessible(true);
                        }
                        ExpandableListView.OnGroupClickListener onGroupClickListener =
                                (ExpandableListView.OnGroupClickListener) mOnGroupClickListenerField.get(view);
                        if (onGroupClickListener != null &&
                                !(onGroupClickListener instanceof WrapperOnGroupClickListener)) {
                            ((ExpandableListView) view).setOnGroupClickListener(
                                    new WrapperOnGroupClickListener(onGroupClickListener));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (view instanceof ListView ||
                        view instanceof GridView) {
                    AdapterView.OnItemClickListener onItemClickListener =
                            ((AdapterView) view).getOnItemClickListener();
                    if (onItemClickListener != null &&
                            !(onItemClickListener instanceof WrapperAdapterViewOnItemClick)) {
                        ((AdapterView) view).setOnItemClickListener(
                                new WrapperAdapterViewOnItemClick(onItemClickListener));
                    }
                }
            }

            if (view instanceof ViewGroup) {
                View targetView = getTargetView((ViewGroup) view, event);
                if (null != targetView) {
                    return targetView;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("all")
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (event != null) {
                int ac = event.getAction() & MotionEvent.ACTION_MASK;
                if (ac == MotionEvent.ACTION_DOWN) {
                    View view = getTargetView((ViewGroup) getRootView(), event);
                    if (view != null) {
                        if (view instanceof AdapterView) {
                            SensorsDataPrivate.trackViewOnClick(view);
                        } else {
                            SensorsDataPrivate.trackViewOnClick(view);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onTouchEvent(event);
    }
}
