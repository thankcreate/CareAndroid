package com.thankcreate.care.control;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * 这个类是用来重载back按键的
 * 如果当前activity中有软键盘出现，第一次按back时只是关软键盘，第二次才返回
 * 用这个类可以一次性返回
 * @author ThankCreate
 */
public class BackKeyLinearLayout extends LinearLayout {

	Activity activity;
	
	public BackKeyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackKeyLinearLayout(Context context) {
		super(context);
	}
	
	public void setActivity(Activity activity)
	{
		this.activity = activity;  
	}

	 @Override
	    public boolean dispatchKeyEventPreIme(KeyEvent event) {
	        if (activity != null && 
	                    event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	            KeyEvent.DispatcherState state = getKeyDispatcherState();
	            if (state != null) {
	                if (event.getAction() == KeyEvent.ACTION_DOWN
	                        && event.getRepeatCount() == 0) {
	                    state.startTracking(event, this);
	                    return true;
	                } else if (event.getAction() == KeyEvent.ACTION_UP
	                        && !event.isCanceled() && state.isTracking(event)) {
	                	activity.onBackPressed();
	                    return true;
	                }
	            }
	        }

	        return super.dispatchKeyEventPreIme(event);
	    }
	
}
