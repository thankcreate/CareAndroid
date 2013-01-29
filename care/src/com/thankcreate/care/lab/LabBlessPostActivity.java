package com.thankcreate.care.lab;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.status.StatusPostActivity;
import com.thankcreate.care.tool.misc.BlessHelper;
import com.thankcreate.care.tool.misc.BlessHelper.PostBlessItemListener;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LabBlessPostActivity extends BaseActivity {
	
	private ActionBar actionBar;
	private EditText editTextName;
	private EditText editTextContent;
	private TextView textCount;
	
	private int maxCount = 60;
	private BlessHelper blessHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_bless_post);
		initActionBar();
		initControl();
	}


	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("发布");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {
				sendClick();
				MobclickAgent.onEvent(LabBlessPostActivity.this, "PostBless");
			}			

			@Override
			public int getDrawable() {
				return R.drawable.thumb_send;				
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	private void initControl() {
		editTextName = (EditText) findViewById(R.id.lab_bless_post_name_input);
		String myName = MiscTool.getMyName();
		if(StringTool.isNullOrEmpty(myName))
		{
			myName = "匿名";
		}
		editTextName.setText(myName);		
		
		editTextContent = (EditText) findViewById(R.id.lab_bless_post_content_input);
		editTextContent.addTextChangedListener(mOnTextChanged);
		editTextContent.requestFocus();
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(maxCount);
		editTextContent.setFilters(FilterArray);
		
		textCount = (TextView) findViewById(R.id.bless_post_count_left);
		textCount.setText(String.valueOf(maxCount));
		
	}
	
	private void sendClick() {
		new AlertDialog.Builder(this)
        .setIcon(R.drawable.thumb_send)
        .setTitle("确认提交？")
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	sendInternal();
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .create().show();
	}
	
	private void sendInternal()
	{		
		String content = editTextContent.getText().toString();
		if(StringTool.isNullOrEmpty(content))
		{
			ToastHelper.show("只有智商超过250才能看见大人写的字么？",true);
			return;
		}
		
		
		if(blessHelper == null)
			blessHelper = new BlessHelper();
		
		actionBar.getProgressBar().post(new Runnable() {			
			@Override
			public void run() {					
				actionBar.setProgressBarVisibility(View.VISIBLE);				
			}
		});
		
		blessHelper.postBlessItem(editTextName.getText().toString(), 
				editTextContent.getText().toString(),
				mPostBlessItemListener);
		
	}
	
	private PostBlessItemListener mPostBlessItemListener = new PostBlessItemListener()
	{
		@Override
		public void postComplete() {
			ToastHelper.show("发送成功", true);	
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
			finish();
		}
	};
	
	private TextWatcher mOnTextChanged = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			int length = s.toString().length();
			textCount.setText(String.valueOf(maxCount - length));
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
		}
	};
}
