package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.aospdatetimepicker.Utils;

/**
 * Created by Phillip Hsu on 8/17/2016.
 */
public abstract class NumbersGrid extends GridLayout implements View.OnClickListener {
    private static final String TAG = "NumbersGrid";
    private static final int COLUMN_COUNT = 3;

    // Package visible so our concrete subclasses (in the same package) can access this.
    OnNumberSelectedListener mSelectionListener;
    View mLastSelectedView;

    final int mSelectedTextColor;
    // TODO: The half-day buttons in the dialog's layout also need to use this color.
    // Consider moving this to either the Dialog class, or move the buttons and the FAB
    // to the GridSelectorLayout class and then move these to GridSelectorLayout.
    int mDefaultTextColor;

    private boolean mIsInitialized;
    private int mSelection; // The number selected from this grid

    public interface OnNumberSelectedListener {
        void onNumberSelected(int number);
    }

    @LayoutRes
    protected abstract int contentLayout();

    public NumbersGrid(Context context) {
        super(context);
        setColumnCount(COLUMN_COUNT);
        inflate(context, contentLayout(), this);
        // We don't do method binding because we don't know the IDs of
        // our subclasses' buttons, if any.
        registerClickListeners();
        mIsInitialized = false;
        mDefaultTextColor = ContextCompat.getColor(context, R.color.text_color_primary_light);
        // The reason we can use the Context passed here and get the correct accent color
        // is that this NumbersGrid is programmatically created by the GridSelectorLayout in
        // its initialize(), and the Context passed in there is from
        // NumberGridTimePickerDialog.getActivity().
        mSelectedTextColor = Utils.getThemeAccentColor(context);
        final View defaultSelectedView = getChildAt(indexOfDefaultValue());
        mSelection = valueOf(defaultSelectedView);
        setIndicator(defaultSelectedView);
    }

    public void initialize(OnNumberSelectedListener listener) {
        if (mIsInitialized) {
            Log.e(TAG, "This NumbersGrid may only be initialized once.");
            return;
        }
        mSelectionListener = listener;
        mIsInitialized = true;
    }

    public int getSelection() {
        return mSelection;
    }

    public void setSelection(int value) {
        mSelection = value;
    }

    /**
     * The default implementation assumes the clicked view is of type TextView,
     * casts the view accordingly, and parses the number from the text it contains.
     * @param v the View that was clicked
     */
    @Override
    public void onClick(View v) {
        setIndicator(v);
        mSelection = valueOf(v);
        mSelectionListener.onNumberSelected(mSelection);
    }

    /**
     * Returns whether the specified View from our hierarchy can have an
     * OnClickListener registered on it. The default implementation
     * checks if this view is of type TextView. Subclasses can override
     * this to fit their own criteria of what types of Views in their
     * hierarchy can have a click listener registered on.
     *
     * @param view a child view from our hierarchy
     */
    protected boolean canRegisterClickListener(View view) {
        return view instanceof TextView;
    }

    /**
     * Sets a selection indicator on the clicked number button. The indicator
     * is the accent color applied to the button's text.
     *
     * @param view the clicked number button
     */
    protected void setIndicator(View view) {
        clearIndicator(); // Does nothing if there was no indicator last selected
        TextView tv = (TextView) view;
        tv.setTextColor(mSelectedTextColor);
        mLastSelectedView = view;
    }

    /**
     * Clear the selection indicator on the last selected view. Clearing the indicator
     * reverts the text color back to its default.
     */
    protected void clearIndicator() {
        if (mLastSelectedView != null) {
            TextView tv = (TextView) mLastSelectedView;
            tv.setTextColor(mDefaultTextColor);
            mLastSelectedView = null;
        }
    }

    /**
     * @return the index for the number button that should have the indicator set on by default.
     * The base implementation returns 0, for the first child.
     */
    protected int indexOfDefaultValue() {
        return 0;
    }

    /**
     * @return the number held by the button parsed into an integer. The base implementation
     * assumes the view is of type TextView.
     */
    protected int valueOf(View button) {
        return Integer.parseInt(((TextView) button).getText().toString());
    }

    /**
     * The default implementation sets the appropriate text color on all of the number buttons
     * as determined by {@link #canRegisterClickListener(View)}.
     */
    void setTheme(Context context, boolean themeDark) {
        mDefaultTextColor = ContextCompat.getColor(context, themeDark?
                R.color.text_color_primary_dark : R.color.text_color_primary_light);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            // TODO: We can move this to the ctor, because this isn't dependent on the theme.
            Utils.setColorControlHighlight(v, mSelectedTextColor/*colorAccent*/);
            // Filter out views that aren't number buttons
            if (canRegisterClickListener(v)) {
                final TextView tv = (TextView) v;
                // Filter out the current selection
                if (mSelection != valueOf(tv)) {
                    tv.setTextColor(mDefaultTextColor);
                }
            }
        }
    }

    /**
     * Iterates through our hierarchy and sets the subclass's implementation of OnClickListener
     * on each number button encountered. By default, the number buttons are assumed to be of
     * type TextView.
     */
    private void registerClickListeners() {
        int i = 0;
        View v;
        while (i < getChildCount() && canRegisterClickListener(v = getChildAt(i))) {
            v.setOnClickListener(this);
            i++;
        }
    }
}
