package com.example.stridecalculation;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter{
	
	private Context context;

	private List<WalkRecord> list;

	public MyAdapter(HistoryActivity context, List<WalkRecord> list) {
		// TODO Auto-generated constructor stub
		super();
		this.context = context;
		this.list = list;
	}

	//getcount  ��ȡ���ݵĸ���
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	//getView  ��Ҫ����һ��View��������ʾ����Դ�е�����
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
			WalkRecord walkRecord = list.get(position);
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup group = (ViewGroup) inflater.inflate(R.layout.walk_record, null);
			TextView dateTodayTextView = (TextView) group.findViewById(R.id.dateToday);
			TextView pathTextView = (TextView) group.findViewById(R.id.path);
			TextView activeTimeTextView = (TextView) group.findViewById(R.id.activeTime);
			TextView distanceTextView = (TextView) group.findViewById(R.id.distance);
			TextView calorieTextView = (TextView) group.findViewById(R.id.calorie);
			
			dateTodayTextView.setText(walkRecord.getDateToday());
			pathTextView.setText(String.valueOf(walkRecord.getPath()));
			activeTimeTextView.setText(String.valueOf(walkRecord.getActiveTime()/6000));
			distanceTextView.setText(String.valueOf(walkRecord.getDistance()/100));
			calorieTextView.setText(String.valueOf(walkRecord.getCalorie()));
			
		return group;
	}

	
	
}