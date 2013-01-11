package com.thankcreate.care.password;

import com.thankcreate.care.MainActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.preference.PreferenceActivity;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PasswordActivity extends Activity {

	private EditText textShow;
	private Button[] buttons = {null, null, null, null, null, null, null, null, null, null};
	private Button btnBack;
	private Button btnUnlock;
	
	private String realPassword = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);
		initControl();
		realPassword = PreferenceHelper.getString("Global_Password");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_password, menu);
		return false;
	}
	
	private void initControl()
	{
		textShow = (EditText) findViewById(R.id.password_input_show);
		
		btnBack = (Button) findViewById(R.id.password_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String input = textShow.getText().toString();
				if(StringTool.isNullOrEmpty(input))
					return;
				input = input.substring(0, input.length() - 1);
				textShow.setText(input);
			}
		});
		
		btnUnlock = (Button) findViewById(R.id.password_unlock);
		btnUnlock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Builder alertDialog = new  AlertDialog.Builder(PasswordActivity.this);
				alertDialog.setTitle("#_#");
				alertDialog.setMessage("大胆刁民，竟然欺负到朕的头上来了");
				alertDialog.setPositiveButton("草民罪该万死，求皇上法外开恩", null);
				alertDialog.show();				
			}
		});
		
		buttons[0] = (Button) findViewById(R.id.password_btn0);
		buttons[1] = (Button) findViewById(R.id.password_btn1);
		buttons[2] = (Button) findViewById(R.id.password_btn2);
		buttons[3] = (Button) findViewById(R.id.password_btn3);
		buttons[4] = (Button) findViewById(R.id.password_btn4);
		buttons[5] = (Button) findViewById(R.id.password_btn5);
		buttons[6] = (Button) findViewById(R.id.password_btn6);
		buttons[7] = (Button) findViewById(R.id.password_btn7);
		buttons[8] = (Button) findViewById(R.id.password_btn8);
		buttons[9] = (Button) findViewById(R.id.password_btn9);
		for(int i = 0; i < buttons.length; i++)
		{
			buttons[i].setOnClickListener(new NumberOnClickListner(i));
		}
	}
	
	class NumberOnClickListner implements OnClickListener
	{
		public int num;
		
		public NumberOnClickListner(int num) {
			super();
			this.num = num;
		}

		@Override
		public void onClick(View v) {
			String input = textShow.getText().toString();
			if(input.length() >= 8)
				return;
			
			input += String.valueOf(num);
			textShow.setText(input);
			if(input.equals(realPassword) || StringTool.isNullOrEmpty(realPassword)){
				Intent intent = new Intent();
				intent.setClass(PasswordActivity.this, MainActivity.class);					
				startActivity(intent);
			}
		}
	}

}
