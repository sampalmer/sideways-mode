/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import samuelpalmer.sensorautorotation.R;

/**
 * Copied and adjusted from the Android 7.1 settings app.
 */
public class SwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {

    public interface OnSwitchChangeListener {
        /**
         * Called when the checked state of the Switch has changed.
         *
         * @param switchView The Switch view whose state has changed.
         * @param isChecked  The new checked state of switchView.
         */
        void onSwitchChanged(Switch switchView, boolean isChecked);
    }
    
    private Switch mSwitch;
    private TextView mTextView;
    
    private ArrayList<OnSwitchChangeListener> mSwitchChangeListeners = new ArrayList<>();
	
	private static int[] XML_ATTRIBUTES = {R.attr.switchBarBackgroundColor};
	
    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
	    init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
	
    private void init(Context context, AttributeSet attrs) {
	    LayoutInflater.from(context).inflate(R.layout.switch_bar, this);
	
	    final TypedArray a = context.obtainStyledAttributes(attrs, XML_ATTRIBUTES);
	    int switchBarBackgroundColor = a.getColor(0, 0);
	    a.recycle();
	
	    mTextView = (TextView) findViewById(R.id.switch_text);
	
	    mSwitch = (Switch) findViewById(R.id.switch_widget);
	    setBackgroundColor(switchBarBackgroundColor);
	    mSwitch.setBackgroundColor(switchBarBackgroundColor);
	
	    mSwitch.setOnCheckedChangeListener(this);
	
	    addOnSwitchChangeListener(new OnSwitchChangeListener() {
		    @Override
		    public void onSwitchChanged(Switch switchView, boolean isChecked) {
			    setTextViewLabel(isChecked);
		    }
	    });
	
	    setOnClickListener(this);
    }
    
    public void setTextViewLabel(boolean isChecked) {
        mTextView.setText(isChecked ? R.string.switchbar_on : R.string.switchbar_off);
    }
    
    public void setChecked(boolean checked) {
        setTextViewLabel(checked);
        mSwitch.setChecked(checked);
    }

    public boolean isChecked() {
        return mSwitch.isChecked();
    }
    
    @Override
    public void onClick(View v) {
        final boolean isChecked = !mSwitch.isChecked();
        setChecked(isChecked);
    }

    public void propagateChecked(boolean isChecked) {
        for (OnSwitchChangeListener listener : mSwitchChangeListeners)
            listener.onSwitchChanged(mSwitch, isChecked);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        propagateChecked(isChecked);
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
        }
        mSwitchChangeListeners.add(listener);
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
        }
        mSwitchChangeListeners.remove(listener);
    }
    
}
