package com.thankcreate.care.password;

import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class PasswordSetActivity extends BaseActivity {

	private ActionBar actionBar;
	private EditText textInput1;
	private EditText textInput2;
	private RelativeLayout layoutConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_set);
		initActionBar();
		initControl();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_password_set, menu);
		return true;
	}
	
	private void initActionBar()
	{		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("启动密码");       
        actionBar.SetTitleLogo(R.drawable.tab_settings);
        addActionBarBackButton(actionBar);
	}
	
	private void initControl()
	{
		textInput1 = (EditText) findViewById(R.id.password_input_1);
		textInput2 = (EditText) findViewById(R.id.password_input_2);
		layoutConfirm = (RelativeLayout) findViewById(R.id.password_confirm);
		layoutConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmClicked();
			}
		});
	}
	
	private void confirmClicked()
	{
		String password1 = textInput1.getText().toString();
		String password2 = textInput2.getText().toString();
		if(StringTool.isNullOrEmpty(password1) || StringTool.isNullOrEmpty(password2))
		{
			Builder alertDialog = new  AlertDialog.Builder(PasswordSetActivity.this);
			alertDialog.setTitle(">_<");
			alertDialog.setMessage("输入为空是想闹哪样的喵~");
			alertDialog.setPositiveButton("寡人喻矣~", null);
			alertDialog.show();
			return;
		}
		
		if(!password1.equals(password2))
		{
			Builder alertDialog = new  AlertDialog.Builder(PasswordSetActivity.this);
			alertDialog.setTitle(">_<");
			alertDialog.setMessage("两次输入不一样的喵~");
			alertDialog.setPositiveButton("寡人喻矣~", null);
			alertDialog.show();
			return;
		}
		
		// 好不容易成功了
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();
    	editor.putString("Global_Password", password1);
    	editor.putString("Global_UsePassword", "True");
    	editor.commit();
    	finish();
	}

}
