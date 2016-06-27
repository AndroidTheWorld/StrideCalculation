package com.jqd.stridecalculation.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����5:11:13
 * @description �Ʋ���ԭ�����ʾ
 */
public class TheoryActivity extends Activity {

	ImageButton imageButtonTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //����ʹ���Զ������ 
		setContentView(R.layout.activity_theory); 
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_setting);//�Զ��岼�ָ�ֵ
		
		TextView textView = (TextView) findViewById(R.id.textViewTitleSetting);
		textView.setText("�Ʋ�ԭ��");
		
		imageButtonTitle = (ImageButton) findViewById(R.id.imageButtonTitleSetting);
		imageButtonTitle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();//���ذ�ť
			}
		});
	}

}
