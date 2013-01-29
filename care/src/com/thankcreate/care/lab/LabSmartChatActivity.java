package com.thankcreate.care.lab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.BlessHelper;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.ChatItemViewModel;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LabSmartChatActivity extends LabShareActivity {

	private EditText editTextInput;
	private Button btnSubmit;
	private ListView listView;
	private SmartChatListAdapter adapter;

	private String herURL;
	private String myURL;
	private String herName;
	private String myName;

	private final int ANSWER_DELAY = 3000;

	private String[] herSentece = { "^_^ 然后呢?", "呵呵..", "嗯嗯，这样~~" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_smart_chat);
		initActionBar();
		initProfile();
		initControl();
		MobclickAgent.onEvent(this, "LabSmartChatActivity");
	}

	private void initProfile() {
		herURL = MiscTool.getHerIconUrl();
		myURL = MiscTool.getMyIconUrl();
		herName = MiscTool.getHerName();
		myName = MiscTool.getMyName();
		if (StringTool.isNullOrEmpty(myName)) {
			ToastHelper.show("请先至少登陆一个帐户");
			finish();
			return;
		}

		if (StringTool.isNullOrEmpty(herName)) {
			ToastHelper.show("请先至少关注一个帐户");
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab_smart_chat, menu);
		return false;
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();
		actionBar.setTitle("非智能聊天");
		actionBar.addActionRight(new Action() {

			@Override
			public void performAction(View view) {
				cleanHistory();
			}

			@Override
			public int getDrawable() {
				return R.drawable.thumb_clean;
			}
		});
	}

	private void initControl() {
		editTextInput = (EditText) findViewById(R.id.lab_chat_input_edittext);
		editTextInput.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN 
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					submit();
				}
				return false;
			}
		});
		btnSubmit = (Button) findViewById(R.id.lab_chat_submit);
		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});
		listView = (ListView) findViewById(R.id.lab_chat_list_view);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		// 读缓存
		List<ChatItemViewModel> listModel = null;
		try {
			File myDir = App.getAppContext().getFilesDir();
			File cacheFile = new File(myDir,
					AppConstants.LAB_SMART_CHAT_HISTORY);
			FileInputStream fis = new FileInputStream(cacheFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			listModel = (ArrayList<ChatItemViewModel>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			listModel = null;
			e.printStackTrace();
		}
		adapter = new SmartChatListAdapter(LabSmartChatActivity.this);
		listView.setAdapter(adapter);
		if (listModel == null) {
			listModel = new ArrayList<ChatItemViewModel>();
		}

		if (listModel.isEmpty()) {
			ChatItemViewModel model = new ChatItemViewModel();
			model.type = ChatItemViewModel.TYPE_HER;
			model.title = herName;
			model.iconURL = herURL;
			model.content = "^_^";
			model.time = new Date();
			listModel.add(model);
		}
		adapter.setListModel(listModel);
		listView.setSelection(adapter.getCount() - 1);
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editTextInput.getWindowToken(), 0);
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private void submit() {
		ChatItemViewModel model = new ChatItemViewModel();
		model.type = ChatItemViewModel.TYPE_ME;
		model.title = myName;
		model.iconURL = myURL;
		model.content = editTextInput.getText().toString();
		model.time = new Date();
		adapter.addItem(model);
		addHerReply();
		editTextInput.setText("");
	}

	/**
	 * 必须确保存这个函数是被UI线程调用的
	 */
	private void addHerReply() {
		final ChatItemViewModel model = new ChatItemViewModel();
		model.type = ChatItemViewModel.TYPE_HER;
		model.title = herName;
		model.iconURL = herURL;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				model.content = ".";
				adapter.addItem(model);
				adapter.refresh();
			}
		}, ANSWER_DELAY * 2 / 5);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				model.content = "..";
				adapter.refresh();
			}
		}, ANSWER_DELAY * 3 / 5);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				model.content = "...";
				adapter.refresh();
			}
		}, ANSWER_DELAY * 4 / 5);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				model.content = "...";
				Random random = new Random();
				int index = random.nextInt(herSentece.length);
				model.content = herSentece[index];
				model.time = new Date();
				adapter.refresh();
			}
		}, ANSWER_DELAY * 5 / 5);
	}

	private void cleanHistory() {
		adapter.clear();
		File a  = App.getAppContext().getFilesDir();
		try {
			App.getAppContext().deleteFile(AppConstants.LAB_SMART_CHAT_HISTORY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 到另一个线程中去写文件，保存聊天历史
		// 读缓存
		try {
			if (adapter == null || adapter.listModel == null
					|| adapter.listModel.size() == 0)
				return;
			File myDir = App.getAppContext().getFilesDir();
			File cacheFile = new File(myDir,
					AppConstants.LAB_SMART_CHAT_HISTORY);
			FileOutputStream fos = new FileOutputStream(cacheFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(adapter.listModel);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class SmartChatListAdapter extends BaseAdapter {

		public List<ChatItemViewModel> listModel = new ArrayList();;
		private LayoutInflater mInflater;

		public SmartChatListAdapter(Context context) {
			super();
			mInflater = LayoutInflater.from(context);
		}

		public void refresh() {
			notifyDataSetChanged();
		}

		public void addItem(ChatItemViewModel model) {
			listModel.add(model);
			notifyDataSetChanged();
		}

		public void setListModel(List<ChatItemViewModel> input) {
			listModel = input;
			notifyDataSetChanged();
		}

		public void clear() {
			listModel.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return listModel.size();
		}

		@Override
		public Object getItem(int position) {
			try {
				return listModel.get(position);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			ChatItemViewModel model = listModel.get(position);
			if (model == null)
				return null;

			if (convertView != null
					&& ((ViewHolder) convertView.getTag()).type == model.type) {
				holder = (ViewHolder) convertView.getTag();
			} else {
				holder = new ViewHolder();
				if (model.type == ChatItemViewModel.TYPE_HER)
					convertView = mInflater.inflate(
							R.layout.listview_item_lab_smart_chat_left, null);
				else
					convertView = mInflater.inflate(
							R.layout.listview_item_lab_smart_chat_right, null);

				holder.imageAvatar = (ImageView) convertView
						.findViewById(R.id.lab_smart_chat_item_avatar_imageview);
				holder.textTitle = (TextView) convertView
						.findViewById(R.id.lab_smart_chat_item_title);
				holder.textContent = (TextView) convertView
						.findViewById(R.id.lab_smart_chat_item_content);
				holder.textTime = (TextView) convertView
						.findViewById(R.id.lab_smart_chat_item_time);
				holder.type = model.type;
				convertView.setTag(holder);
			}
			holder.imageAvatar.setTag(model.iconURL);
			App.getDrawableManager().fetchDrawableOnThread(model.iconURL,
					holder.imageAvatar);
			holder.textTitle.setText(model.title);
			holder.textContent.setText(model.content);
			holder.textTime.setText(DateTool
					.convertDateToStringInShow(model.time));
			return convertView;
		}

		public class ViewHolder {
			public ImageView imageAvatar;
			public TextView textTitle;
			public TextView textContent;
			public TextView textTime;
			public int type;
		}
	}
	
	

	@Override
	protected void preShare() {
		super.preShare();		
	}

	@Override
	protected String getShareTextSinaWeibo() {
		String name = MiscTool.getMyName(EntryType.SinaWeibo);
		String preContentString =  String.format("UP主活了这么多年， @%s 是我见过的最无聊的一个，没有之一！", 
				name);
		return preContentString;
	}

	@Override
	protected String getShareTextRenren() {
		String name = MiscTool.getMyName(EntryType.Renren);
		String ID = MiscTool.getMyID(EntryType.Renren);
		String preContentString =  String.format("UP主活了这么多年， @%s(%s) 是我见过的最无聊的一个，没有之一！", 
				name, ID);
		return preContentString;
	}

	@Override
	protected String getShareTextDouban() {
		String name = MiscTool.getMyName(EntryType.Douban);
		String preContentString =  String.format("UP主活了这么多年， @%s 是我见过的最无聊的一个，没有之一！", 
				name);
		return preContentString;
	}
}