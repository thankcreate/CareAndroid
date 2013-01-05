package com.thankcreate.care.tool.ui;

import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewTool {

	public static void setListViewHeightBasedOnChildren(ListView listView,
			int extra, Boolean isEqualHeight) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;

		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
				MeasureSpec.AT_MOST);

		if(!isEqualHeight)
		{
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				if (listItem instanceof ViewGroup)
					listItem.setLayoutParams(new LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
				totalHeight += listItem.getMeasuredHeight();
			}
		}
		else
		{
			int firstHeight = 0;
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				if (listItem instanceof ViewGroup)
					listItem.setLayoutParams(new LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
				firstHeight += listItem.getMeasuredHeight();
				break;
			}
			totalHeight += listAdapter.getCount() * firstHeight;
		}

		totalHeight += extra;
		int test1 = listView.getDividerHeight() ;
		int test2 =  listAdapter.getCount();
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
}
