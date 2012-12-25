package com.thankcreate.care.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;
import com.thankcreate.care.R;

/***
* 自定义listview
* 
* @author Administrator
* 
*/
public class RoundCornerListView extends ListView {
	
public RoundCornerListView(Context context) {
  super(context);
}
public RoundCornerListView(Context context, AttributeSet attrs) {
  super(context, attrs);
}
public RoundCornerListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
}



@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int itemnum = pointToPosition(x, y);

			if (itemnum == AdapterView.INVALID_POSITION)
				break;
			else 
			{
				if (itemnum == 0)
				{
					if (itemnum == (getAdapter().getCount() - 1))
					{					
						setSelector(R.drawable.selector_bkg_table_onlyone_round_corner);
					} 
					else 
					{						
						setSelector(R.drawable.selector_bkg_table_firstrow_round_corner);
					}
				} 
				else if (itemnum == (getAdapter().getCount() - 1))
				{					
					setSelector(R.drawable.selector_bkg_table_lastrow_round_corner);
				}					
				else 
				{				
					setSelector(R.drawable.selector_bkg_table_middlerow_round_corner);
				}
			}

			break;			
		}
    return super.onInterceptTouchEvent(ev);
}


}