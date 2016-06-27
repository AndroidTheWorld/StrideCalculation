package com.jqd.stridecalculation.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����5:10:47
 * @description ע���������ʾ
 */
public class NoteActivity extends Activity {

	ImageButton imageButtonTitleNoteButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //����ʹ���Զ������ 
		setContentView(R.layout.activity_note); 
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_setting);//�Զ��岼�ָ�ֵ
		
		TextView textView = (TextView) findViewById(R.id.textViewTitleSetting);
		textView.setText("ע������");
		
		imageButtonTitleNoteButton = (ImageButton) findViewById(R.id.imageButtonTitleSetting);
		imageButtonTitleNoteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();//���ذ�ť
			}
		});
		
	}


}
