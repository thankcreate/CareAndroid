package com.thankcreate.care.control;


import com.thankcreate.care.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SearchBarWidget extends LinearLayout {

	private ImageButton mSearchCancelButton;
	//private ImageView mSearchRightImageView;
	private EditText mSearchEditText;
	
	private onSearchListener mOnSearchListener = null; 
	public interface onSearchListener
	{
		public void onSearchChange(String search);
	}
	
	public SearchBarWidget(Context context)
	{
		super(context);
		viewInit(context);
		logicInit();
	}
	
	public SearchBarWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		viewInit(context);
		logicInit();
	}
	
	/*** 界面初始化 **/
	private void viewInit(Context context)
	{
		inflate(context,R.layout.search_bar_layout, this);
		mSearchCancelButton = (ImageButton) findViewById(R.id.search_cancel_button);//		
		mSearchEditText = (EditText)findViewById(R.id.search_text);
		setTextEditable(true);
	}
	
	/*** 逻辑初始化 **/
	private void logicInit()
	{
		if(mSearchCancelButton != null)
		{
			mSearchCancelButton.setOnClickListener(mSearchCancelClickListener);
		}
		
		if(mSearchEditText != null)
		{
			mSearchEditText.setOnTouchListener(mSearchEditTextOnClickListener);
			mSearchEditText.addTextChangedListener(mSearchTextWatcher);
		}
		setTextEditable(false);
	}
	
	/** 取消键点击事件处理 **/
	private View.OnClickListener mSearchCancelClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//mSearchCancelButton.setVisibility(View.GONE);
			InputMethodManager imm = (InputMethodManager)getContext().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);		
				
			
//			mSearchEditText.setText("");
//			mSearchEditText.clearFocus();
//			mSearchEditText.setFocusable(false);
			   
			if(mSearchCancelButton != null && mSearchCancelButton.getVisibility() == View.VISIBLE)
			{
				setSearchBarState(LAYOUT_STATE_VIEW);
			}
		}
	};
	
	/** EditText Touch事件处理 **/
	private View.OnTouchListener mSearchEditTextOnClickListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(mSearchCancelButton!= null  
					&& mSearchCancelButton.getVisibility() != View.VISIBLE)
			{
				setSearchBarState(LAYOUT_STATE_EDIT);
			}
			return false;
		}
	};
	
	/** 搜索条文字变化监听器 ***/
	private TextWatcher mSearchTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s.toString().length() == 0) {
				mSearchCancelButton.setVisibility(View.GONE);	
			}
			else {
				mSearchCancelButton.setVisibility(View.VISIBLE);
			}
			if(mOnSearchListener != null)
			{
				mOnSearchListener.onSearchChange(s.toString());
			}	
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub	
		}
	};
	
	/***
	 * 设置搜索框是否可以编辑
	 * @param isEditable
	 */
	private void setTextEditable(boolean isEditable)
	{
		if(isEditable)
		{
			mSearchEditText.setFocusableInTouchMode(true);
			mSearchEditText.setFocusable(true);
			mSearchEditText.requestFocus();
		}
		else
		{
			mSearchEditText.clearFocus();
			mSearchEditText.setFocusable(false);
		}
	}
	
	private static final int LAYOUT_STATE_VIEW = 1;
	private static final int LAYOUT_STATE_EDIT = 2;
	/**
	 * 设置搜索条的状态
	 * <p>浏览状态 LAYOUT_STATE_VIEW 只显示搜索条 同时失去焦点</p>
	 * <p>编辑状态 LAYOUT_STATE_EDIT 显示搜索条和取消按钮 获取焦点</p>
	 * @param state
	 */
	private void setSearchBarState(int state)
	{
		switch (state) {
		case LAYOUT_STATE_VIEW:
			
			mSearchEditText.setText("");
			setTextEditable(false);
			mSearchCancelButton.setVisibility(View.GONE);			
			
			break;
		case LAYOUT_STATE_EDIT:

			setTextEditable(true);			
			
			break;

		default:
			break;
		}
	}

	
	public void setOnSearchListener(onSearchListener listener)
	{
		if(listener != null)
		{
			mOnSearchListener = listener;
		}
	}

}