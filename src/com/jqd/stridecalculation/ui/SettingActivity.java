package com.jqd.stridecalculation.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jqd.stridecalculation.ad.BannerAdManager;
import com.jqd.stridecalculation.task.BackgroundService;
import com.qq.e.ads.AdRequest;
import com.qq.e.ads.AdSize;
import com.qq.e.ads.AdView;
import com.qq.e.appwall.GdtAppwall;

public class SettingActivity extends Activity {

	String TAG = "settingActivity";
	
	private ImageButton imageButtonTitleSettingButton;
	private LinearLayout item1;
	private LinearLayout item2;
	private LinearLayout item3;
	private LinearLayout item4;
	private TextView pathLengthTextView;
	private TextView weightTextView;
	private TextView degreeTextView;
	private TextView workTypeTextView;
	private AlertDialog settingDialog;
	private AlertDialog alertDialog;
	
	private float userWeight=50;
	private float pathLength=50;
	private int openBackgroundService=1;
	private float sensorDegree=50;
	
	private boolean lockType2=true;
	private boolean lockType3=true;
	private boolean lockType4=true;
	private SharedPreferences lock_info;

	private AlertDialog connectDialog;
	GdtAppwall appwall;
	BannerAdManager bannerAdManager;
	private boolean hideAD2 = true;
	
	public RelativeLayout extraLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //����ʹ���Զ������ 
		setContentView(R.layout.activity_setting); 
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_setting);//�Զ��岼�ָ�ֵ 
		
		appwall = new GdtAppwall(this, "1102312596", "9030705060756483", true);
		
		imageButtonTitleSettingButton = (ImageButton) findViewById(R.id.imageButtonTitleSetting);
		imageButtonTitleSettingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();//���ذ�ť
			}
		});
		
		item1 = (LinearLayout) findViewById(R.id.item1);
		item2 = (LinearLayout) findViewById(R.id.item2);
		item3 = (LinearLayout) findViewById(R.id.item3);
		item4 = (LinearLayout) findViewById(R.id.item4);

		item1.setOnClickListener(itemsOnClickListener);
		item2.setOnClickListener(itemsOnClickListener);
		item3.setOnClickListener(itemsOnClickListener);
		item4.setOnClickListener(itemsOnClickListener);
		
		pathLengthTextView = (TextView) findViewById(R.id.textViewSetting7);
		weightTextView = (TextView) findViewById(R.id.textViewSetting4);
		degreeTextView = (TextView) findViewById(R.id.textViewSetting10);
		workTypeTextView = (TextView) findViewById(R.id.textViewWorkType);
		extraLayout = (RelativeLayout) findViewById(R.id.extraLayout);
		
		lock_info = getSharedPreferences("test", MODE_PRIVATE);
		hideAD2 = lock_info.getBoolean("hideAD2", true);
	}

	protected void onStart() {
		readDatabase();
		disp();
		if (!hideAD2) {
			bannerAdManager = new BannerAdManager();
			bannerAdManager.showBannerAD(SettingActivity.this);
		}else {
			SharedPreferences.Editor prefEditor = lock_info.edit();
			prefEditor.putBoolean("hideAD2", false);
			prefEditor.apply();
		}
		super.onStart();
	}

	View.OnClickListener itemsOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(SettingActivity.this, BackgroundService.class);
			stopService(intent);
			switch (v.getId()) {
			case R.id.item1:
				setWorkType();
				break;
			case R.id.item2:
				editPathLength();
				break;
			case R.id.item3:
				editUserWeight();
				break;
			case R.id.item4:
				editSensorDegree();
				break;
			default:
				break;
			}
		}
	};
	
	private String[] ss=new String[] {
			"�رպ�̨�Ʋ�",
			"��������ģʽ���ֻ�δ����ʱ�Ʋ���",
			"ģ������ģʽ���Ƽ���",
			"��̨ǿ�ƼƲ�ģʽ�������ֻ���֧�֣�",
			"��̨���ܽڵ�ģʽ�������ֻ���֧�֣�"
		};
	private String[] ss2=new String[] {
			"�ѹر�",
			"��������ģʽ",
			"ģ������ģʽ",
			"��̨ǿ�ƼƲ�ģʽ",
			"��̨���ܽڵ�ģʽ"
		};
	public void setWorkType() {
			
			AlertDialog.Builder builder = new Builder(this);
				builder.setTitle("���üƲ�ģʽ");
			builder.setSingleChoiceItems(ss, openBackgroundService, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					lockType2 = lock_info.getBoolean("lockType2", true);
					lockType3 = lock_info.getBoolean("lockType3", true);
					lockType4 = lock_info.getBoolean("lockType4", true);
					alertDialog.dismiss();
					if (which==0 || which==1) {
						workTypeTextView.setText(ss2[which]);
						Toast.makeText(SettingActivity.this, ss2[which], Toast.LENGTH_SHORT).show();
						openBackgroundService = which;
						writeDatabase();
					}

					//�ж��Ƿ�����ù���,��δ��������ʾ����Ӧ��ǽ
					switch (which) {
					case 2:
						if (lockType2) {
							goToAppWall(which);
						}else {
							workTypeTextView.setText(ss2[which]);
							Toast.makeText(SettingActivity.this, ss2[which], Toast.LENGTH_SHORT).show();
							openBackgroundService = which;
							writeDatabase();
						}
						break;
					case 3:
						if (lockType3) {
							goToAppWall(which);
						}else {
							workTypeTextView.setText(ss2[which]);
							Toast.makeText(SettingActivity.this, ss2[which], Toast.LENGTH_SHORT).show();
							openBackgroundService = which;
							writeDatabase();
						}
						break;
					case 4:
						if (lockType4) {
							goToAppWall(which);
						}else {
							workTypeTextView.setText(ss2[which]);
							Toast.makeText(SettingActivity.this, ss2[which], Toast.LENGTH_SHORT).show();
							openBackgroundService = which;
							writeDatabase();
						}
						break;

					default:
						break;
					}

				}
			});
			alertDialog = builder.create();
			alertDialog.show();

	}
	
	public void goToAppWall(final int which) {
		
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		//mobile 3G Data Network
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(mobile==State.CONNECTED||mobile==State.CONNECTING || wifi==State.CONNECTED||wifi==State.CONNECTING){
			//����������
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("��Ǹ����ģʽ��δ����");
			ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_for_new_users,null);
			Button iKnowButton = (Button) dialogSettingGroup.findViewById(R.id.iKnowButton);
			Button notShowAgainButton = (Button) dialogSettingGroup.findViewById(R.id.notShowAgainButton);
			TextView locklockTextView = (TextView) dialogSettingGroup.findViewById(R.id.textViewForNewUsers);
			locklockTextView.setText("����Ӧ��ǽ������һ��Ӧ�ü������ý�����ģʽ.");
			iKnowButton.setText("�´���˵");
			notShowAgainButton.setText("��������");
			
			iKnowButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					connectDialog.dismiss();
				}
			});
			notShowAgainButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					connectDialog.dismiss();
					SharedPreferences.Editor prefEditor = lock_info.edit();
					switch (which) {
					case 2:
						prefEditor.putBoolean("lockType2", false);
						break;
					case 3:
						prefEditor.putBoolean("lockType3", false);
						break;
					case 4:
						prefEditor.putBoolean("lockType4", false);
						break;

					default:
						break;
					}
					//�����ͳ��׽���ĳһ�ּƲ�ģʽ�ˣ�Ȼ�����Ӧ��ǽ
					prefEditor.apply();
					
					//����Ӧ��ǽ..............................
					appwall.doShowAppWall();
				}
			});

			builder.setView(dialogSettingGroup);
			connectDialog = builder.create();
			connectDialog.show();

			
		}else {
			//δ��������
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("��Ǹ����ģʽδ����");
			ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_for_new_users,null);
			Button iKnowButton = (Button) dialogSettingGroup.findViewById(R.id.iKnowButton);
			Button notShowAgainButton = (Button) dialogSettingGroup.findViewById(R.id.notShowAgainButton);
			TextView locklockTextView = (TextView) dialogSettingGroup.findViewById(R.id.textViewForNewUsers);
			locklockTextView.setText("����Ӧ��ǽ������һ��Ӧ�ü������ý�����ģʽ.\n��⵽��ǰδ�������磡");
			iKnowButton.setText("�´���˵");
			notShowAgainButton.setText("��������");
			iKnowButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					connectDialog.dismiss();
				}
			});
			notShowAgainButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					connectDialog.dismiss();
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //�����ֻ��е�wifi�������ý���
				}
			});

			builder.setView(dialogSettingGroup);
			connectDialog = builder.create();
			connectDialog.show();
		}
		
	}
	
	public void editPathLength () {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("���ò�����(cm)");
		ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_setting,null);
		final EditText settingEditTextSetting = (EditText) dialogSettingGroup.findViewById(R.id.editTextSetting);
		Button confirmButton = (Button) dialogSettingGroup.findViewById(R.id.confirmButton);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (settingEditTextSetting.getText().toString().length()!=0) {
					float temp = Float.valueOf(settingEditTextSetting.getText().toString());
					if (temp>=0 && temp<=500) {
						pathLength = temp;
						disp();
						writeDatabase();
					}else {
						Toast.makeText(SettingActivity.this, "������Ĳ���������Ŷ��", Toast.LENGTH_SHORT).show();
					}
					
				}
				settingDialog.dismiss();
			}
		});
		
		builder.setView(dialogSettingGroup);
		settingDialog = builder.create();
		settingDialog.show();
	}
	
	public void editUserWeight() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("�������أ�(kg)");
		ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_setting,null);
		final EditText settingEditTextSetting = (EditText) dialogSettingGroup.findViewById(R.id.editTextSetting);
		Button confirmButton = (Button) dialogSettingGroup.findViewById(R.id.confirmButton);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (settingEditTextSetting.getText().toString().length()!=0) {
					float temp = Float.valueOf(settingEditTextSetting.getText().toString());
					if (temp>=0 && temp<=500) {
						userWeight = temp;
						disp();
						writeDatabase();
					}else {
						Toast.makeText(SettingActivity.this, "����������ز�����Ŷ��", Toast.LENGTH_SHORT).show();
					}
					
				}
				settingDialog.dismiss();
			}
		});
		
		builder.setView(dialogSettingGroup);
		settingDialog = builder.create();
		settingDialog.show();
	}
	
	public void editSensorDegree() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("���������ȣ�");
		builder.setMessage("��ֵԽ�ͣ������̶�Խ��\nĬ��40 (�Ƽ�30-50)");
		ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater().inflate(R.layout.dialog_setting,null);
		final EditText settingEditTextSetting = (EditText) dialogSettingGroup.findViewById(R.id.editTextSetting);
		Button confirmButton = (Button) dialogSettingGroup.findViewById(R.id.confirmButton);
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (settingEditTextSetting.getText().toString().length()!=0) {
					sensorDegree = Float.valueOf(settingEditTextSetting.getText().toString());
					disp();
					writeDatabase();
				}
				settingDialog.dismiss();
			}
		});
		
		builder.setView(dialogSettingGroup);
		settingDialog = builder.create();
		settingDialog.show();
	}

	
    //���ݿ�----������ʷ��¼���Լ�activity��service���л�ʱ�����ݴ洢�����
    public class DatabaseHelper extends SQLiteOpenHelper{

    	private static final String DB_NAME = "mydata.db"; //���ݿ�����
        private static final int version = 1; //���ݿ�汾
         
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, version);
            // TODO Auto-generated constructor stub
        }
     
        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table pathToday(" +
            		"dateToday varchar(20) primary key, " +
            		"path INTEGER," +
            		"activeTime INTEGER," +
            		"calorie INTEGER," +
            		"fatCost INTEGER," +
            		"distance INTEGER," +
            		"userWeight float,"+
            		"pathLength float,"+
            		"openBackgroundService INTEGER);";       
            db.execSQL(sql);
            sql = "create table userData(" +
            		"pathLength float,"+
            		"userWeight float,"+
            		"sensorDegree float,"+
            		"openBackgroundService INTEGE);";          
            db.execSQL(sql);
            sql = "insert into userData(pathLength, userWeight, sensorDegree, openBackgroundService) values(?,?,?,?);";          
			db.execSQL(sql, new String[]{
					String.valueOf(pathLength), 
					String.valueOf(userWeight), String.valueOf(sensorDegree), 
					String.valueOf(openBackgroundService)});// �������ݲ��洢
//            sql = "insert into pathToday(dateToday, path, activeTime, carlorie, fatCost, distance, userWeight,pathLength) values(?, 10,20,30,40,50,60,70);";          
//            db.execSQL(sql);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
     
        }
    }
	public void readDatabase() {

		DatabaseHelper database = new DatabaseHelper(this);
		SQLiteDatabase myDatabase = null;
		myDatabase = database.getWritableDatabase();

		Cursor cursor = myDatabase.query("userData", null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			userWeight = cursor.getFloat(cursor.getColumnIndex("userWeight"));
		    pathLength = cursor.getFloat(cursor.getColumnIndex("pathLength"));
		    sensorDegree = cursor.getFloat(cursor.getColumnIndex("sensorDegree"));
		    openBackgroundService = cursor.getInt(cursor.getColumnIndex("openBackgroundService"));
////		    Log.i("weight=", String.valueOf(userWeight));
//		    Log.i("pathLength=", String.valueOf(pathLength));
//		    Log.i("sensor=", String.valueOf(sensorDegree));
//		    Log.i("openBackgroundService=", String.valueOf(openBackgroundService));
		}
		
		cursor.close();
		myDatabase.close();
	}
	
	public void writeDatabase() {
//		Log.i(TAG, "writeDatabase");
		DatabaseHelper database = new DatabaseHelper(this);
		SQLiteDatabase myDatabase = null;
		myDatabase = database.getWritableDatabase();
		Cursor cursor = myDatabase.query("userData", null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			String sql = "update [userData] set pathLength = ?, userWeight =?, " +
					"sensorDegree=?, openBackgroundService=?";//�޸ĵ�SQL���
			myDatabase.execSQL(sql, new String[]{String.valueOf(pathLength), 
					String.valueOf(userWeight), String.valueOf(sensorDegree), 
					String.valueOf(openBackgroundService)});//ִ���޸�,�������ݴ洢
		}
		cursor.close();
		myDatabase.close();
	}
	
	public void disp(){
		pathLengthTextView.setText(String.valueOf(pathLength)+"cm");
		weightTextView.setText(String.valueOf(userWeight)+"kg");
		degreeTextView.setText(String.valueOf(sensorDegree));
		workTypeTextView.setText(ss2[openBackgroundService]);
	}
	
	
}
