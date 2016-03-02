package com.example.stridecalculation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.qq.e.appwall.GdtAppwall;
import com.tencent.stat.StatService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button startButton;
	private Button stopButton;
	private Button pauseButton;
	private Button continueButton;
	private Button lockScreenButton;
	private ImageButton imageButtonMenu;
	private ImageButton imageButtonNaruto;
	private TextView textView1;
	private TextView textView3;
	private TextView textView12;
	private TextView textView4;
	private TextView textView5;
	private TextView textView10;
	private TextView textView11;
	private TextView textView15;
	private SeekBar seekBar;
	private ProgressBar progressBar;
	AlertDialog menuDialog;
	View menuView;
	GridView menuGrid;
	int[] menu_image_array = { R.drawable.ic_menu_recent_history,
			R.drawable.ic_menu_view, R.drawable.ic_menu_preferences,
			R.drawable.ic_menu_help, R.drawable.ic_menu_info_details,
			R.drawable.ic_menu_favorite };
	/** �˵����� **/
	String[] menu_name_array = { "��ʷ��¼", "ע������", "����", "�Ʋ�ԭ��", "����", "����" };

	private SensorManager sensorManager;
	private Sensor sensor;
	private float bright_careful = 0.005f;
	private int DELAY_TIME = 200; // 2sû�м�⵽�����Զ��رռ�ʱ��
	private boolean startAdd = false;
	private long timeOfTheDay = 0;
	private long timeOfPathStart = 0;
	private long timeOfPathEnd = 0;
	private long activeTime = 0;
	private long activeTimeLast = 0;
	private Thread timeThread;
	private int pathAtOneTime = 0; // �����Ĳ�����
	private int DELAY_NUM = 1;// ����⵽���ǵ�����һ��������Ϊ�ǶԵģ������ټ�ȥ

	private float userWeight = 50f;// kg
	private float pathLength = 60.0f;// cm
	private int calorie = 0;
	private int distance = 0;
	private int fatCost = 0;
	private String dateToday;
	private boolean lockScreen = false;
	private int mDay;
	private Calendar c;
	private int barStart = 100;

	PowerManager.WakeLock mWakeLock = null;

	private int openBackgroundService = 0;

	String TAG = "MainActivity";

	private float values0 = 0;
	private float values1 = 0;
	private float values2 = 0;

	private AsyncMain asyncMain;
	private boolean cancelThread = false;
	private boolean startSensor = false;
	private float sensorDegree = 40;
	private boolean firstStart = true;
	private boolean firstCreat = true;
	private float screenBrightnessLock = 0.01f;
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private SharedPreferences settings;
	private boolean isNewUser = true;
	private AlertDialog forNewUsersDialog;
	private GdtAppwall appwall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,
				FLAG_HOMEKEY_DISPATCHED);// ��������home��
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);// �Զ��岼�ָ�ֵ

		StatService.trackCustomEvent(this, "onCreate", "");

		appwall = new GdtAppwall(this, "1102312596", "9030705060756483", false);
		settings = getSharedPreferences("test", MODE_PRIVATE);
		isNewUser = settings.getBoolean("isNewUser", true);

		// Log.i(TAG, "MainActivity Creat!");
		// ʵ�������
		textView1 = (TextView) findViewById(R.id.textView1);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView10 = (TextView) findViewById(R.id.textView10);
		textView12 = (TextView) findViewById(R.id.textView12);
		textView11 = (TextView) findViewById(R.id.textView11);
		textView15 = (TextView) findViewById(R.id.textView15);
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		pauseButton = (Button) findViewById(R.id.pauseButton);
		continueButton = (Button) findViewById(R.id.continueButton);
		lockScreenButton = (Button) findViewById(R.id.buttonLockScreen);
		imageButtonMenu = (ImageButton) findViewById(R.id.imageButtonMenu);
		imageButtonNaruto = (ImageButton) findViewById(R.id.imageButtonNaruto);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		seekBar = (SeekBar) findViewById(R.id.seekBar1);

		creatMenu();

		// ����󶨼�����
		startButton.setOnClickListener(clickListener);
		stopButton.setOnClickListener(clickListener);
		pauseButton.setOnClickListener(clickListener);
		continueButton.setOnClickListener(clickListener);
		// ������ʵ����
		// Log.i(TAG, "������ʵ����");
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() == 0) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("��Ǹ!");
			ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater()
					.inflate(R.layout.dialog_for_new_users, null);
			Button iKnowButton = (Button) dialogSettingGroup
					.findViewById(R.id.iKnowButton);
			Button notShowAgainButton = (Button) dialogSettingGroup
					.findViewById(R.id.notShowAgainButton);
			TextView noSensorTextView = (TextView) dialogSettingGroup
					.findViewById(R.id.textViewForNewUsers);

			noSensorTextView.setText("�����豸�в����ڼ��ٶȴ��������޷�ʹ�ñ�Ӧ��.");
			iKnowButton.setText("�ر�");
			notShowAgainButton.setText("����Ӧ��");
			iKnowButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					forNewUsersDialog.dismiss();
					finish();
				}
			});
			notShowAgainButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					forNewUsersDialog.dismiss();
					appwall.doShowAppWall();
				}
			});

			builder.setView(dialogSettingGroup);
			forNewUsersDialog = builder.create();
			forNewUsersDialog.show();
		} else {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		// Log.i(TAG, "������ʵ����");

		// �˵����˳���ѡ��
		// ģ������
		lockScreenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				seekBar.setVisibility(View.VISIBLE);
				v.setVisibility(View.INVISIBLE);
				lockScreen = true;
				seekBar.setProgress(0);
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.screenBrightness = screenBrightnessLock;// ��������ʱ������
				getWindow().setAttributes(lp);

				acquireWakeLock();// ��ȡ��Դ�������ֳ���

			}
		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int progress = seekBar.getProgress();
				if (progress >= 95 && barStart <= 5) {
					seekBar.setProgress(100);
					lockScreen = false;
					seekBar.setVisibility(View.INVISIBLE);
					lockScreenButton.setVisibility(View.VISIBLE);
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.screenBrightness = -1;
					getWindow().setAttributes(lp);

					releaseWakeLock();// �ͷŵ�Դ��
				} else {

					seekBar.setProgress(0);

				}
				// Log.i("stop", String.valueOf(barStart));
				barStart = 100;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

				// WindowManager.LayoutParams lp = getWindow().getAttributes();
				// lp.screenBrightness = 0.1f;
				// getWindow().setAttributes(lp);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (seekBar.getProgress() >= 40 && seekBar.getProgress() <= 60) {
					barStart = 0;
				}
			}
		});

		if (isNewUser) { // ����ǵ�һ��ʹ�����������˵���Ի���
		// Log.i(TAG, "����˵���Ի���");
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("��ӭʹ���ֻ��Ʋ�����!");
			ViewGroup dialogSettingGroup = (ViewGroup) this.getLayoutInflater()
					.inflate(R.layout.dialog_for_new_users, null);
			Button iKnowButton = (Button) dialogSettingGroup
					.findViewById(R.id.iKnowButton);
			Button notShowAgainButton = (Button) dialogSettingGroup
					.findViewById(R.id.notShowAgainButton);

			iKnowButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					forNewUsersDialog.dismiss();
				}
			});
			notShowAgainButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					SharedPreferences.Editor prefEditor = settings.edit();
					prefEditor.putBoolean("isNewUser", false);
					prefEditor.apply();
					forNewUsersDialog.dismiss();
				}
			});

			builder.setView(dialogSettingGroup);
			forNewUsersDialog = builder.create();
			forNewUsersDialog.show();
		}

		c = Calendar.getInstance();
		mDay = c.get(Calendar.DAY_OF_MONTH);
		asyncMain = new AsyncMain();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

		super.onStart();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		// �洢���ݣ�������������Ʋ�,�ر��̣߳�ȡ������������
		// Log.i(TAG, "MainActivity Pause!");
		writeDatabase();
		if (openBackgroundService != 0) {
			Intent intent = new Intent(MainActivity.this,
					BackgroundService.class);
			startService(intent);
		}
		cancelThread = true;
		// if (!startSensor) {
		// sensorManager.unregisterListener(sensorEventListener);
		// }

		releaseWakeLock();

		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// ��ȡ����

		dateToday = c.get(Calendar.YEAR) + "��" + (c.get(Calendar.MONTH) + 1)
				+ "��" + c.get(Calendar.DAY_OF_MONTH) + "��";
		textView15.setText(dateToday);

		firstStart = true;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// ��ʼ�첽�ļ�ʱ��
		cancelThread = false;
		// Log.i(TAG, "MainActivity Resume!");
		timeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!cancelThread) {
						Thread.sleep(10);// ˯10ms
						timeOfTheDay++;
						// Log.i("timeofTheday", String.valueOf(timeOfTheDay));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		timeThread.start();
		// �رշ�����ʾ���������첽����
		Intent intent = new Intent(this, BackgroundService.class);
		stopService(intent);
		asyncMain = new AsyncMain();
		asyncMain.execute();

		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
	}

	// ���η����˳�
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!lockScreen) {
			if (keyCode == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				if ((System.currentTimeMillis() - exitTime) > 2000) {
					Toast.makeText(getApplicationContext(), "�ٴε�������ء��˳�",
							Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else {
					finish();
					// System.exit(0); ִ������Ļ��رյıȽϺݣ��������Ondestroy������,
				}
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_HOME) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// ģ��home������Ϊhome�Ĺ��ܱ������ˣ�
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}

			return super.onKeyDown(keyCode, event);
		} else {
			if (keyCode == KeyEvent.KEYCODE_POWER) {// ģ���Դ������Ϊ��Դ���Ĺ��ܱ������ˣ�
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				// Log.i(TAG, "�ǺǺǺ�");
				lp.screenBrightness = bright_careful;// ��������ʱ������
				if (bright_careful == 0.005f) {
					bright_careful = -1;
				} else {
					bright_careful = 0.005f;
				}
				getWindow().setAttributes(lp);
			}
			return true;// ���ΰ���
		}

	}

	// button---listener
	OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			if (!lockScreen) {

				switch (v.getId()) {
				case R.id.startButton:
					textView12.setText("�Ʋ���");
					startSensor = true;
					progressBar.setVisibility(View.VISIBLE);
					startButton.setVisibility(View.INVISIBLE);
					pauseButton.setVisibility(View.VISIBLE);
					sensorManager.registerListener(sensorEventListener, sensor,
							SensorManager.SENSOR_DELAY_FASTEST);// ע�ᴫ����������
					break;
				case R.id.pauseButton:
					textView12.setText("��ͣ��");
					startSensor = false;
					startAdd = false;
					progressBar.setVisibility(View.INVISIBLE);
					pauseButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.VISIBLE);
					sensorManager.unregisterListener(sensorEventListener,
							sensor);// ȡ������

					break;
				case R.id.continueButton:
					textView12.setText("�Ʋ���");
					startSensor = true;
					progressBar.setVisibility(View.VISIBLE);
					pauseButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.INVISIBLE);
					sensorManager.registerListener(sensorEventListener, sensor,
							SensorManager.SENSOR_DELAY_FASTEST);// ע�ᴫ����������

					break;
				case R.id.stopButton:
					textView12.setText("δ��ʼ");
					startSensor = false;
					startAdd = false;
					progressBar.setVisibility(View.INVISIBLE);
					startButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.INVISIBLE);
					sensorManager.unregisterListener(sensorEventListener,
							sensor);// ȡ������
					path = 0;
					pathAtOneTime = 0;
					activeTime = 0;
					activeTimeLast = 0;
					dispActiveTime();
					dispPath();
					dispCalorieAndDistance();
					break;

				default:
					break;
				}

			}
		}
	};

	SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			values0 = event.values[0];
			values1 = event.values[1];
			values2 = event.values[2];
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};

	private float x = 0;
	private float y = 0;
	private float z = 0;
	private float actualZ0 = 0f;
	private float actualZ;
	private float[] finalZ = { 0f, 0f, 0f };
	private float value0, value1, value2;

	private float gate = 4.0f;
	private float gate_f = 15.0f;
	private float max = 0;
	private float min = 0;
	private int maxi = 0;
	private int mini = 0;
	private boolean waittingH = true;
	private int path = 0;

	private int index = 1;

	public void strideCalculation(float data1, float data2, float data3) {

		if (index <= 10) {
			index++;
		}
		if (index >= 2) {
			x = (38 * x + value0 + data1) / 40;
			y = (38 * y + value1 + data2) / 40;
			z = (38 * z + value2 + data3) / 40;
			actualZ = (x * data1 + y * data2 + z * data3) / 10;
		}
		value0 = data1;
		value1 = data2;
		value2 = data3;

		if (index >= 3) {
			finalZ[0] = finalZ[1];
			finalZ[1] = finalZ[2];
			finalZ[2] = (70 * finalZ[2] + 5 * actualZ0 + 5 * actualZ) / 80;
		}

		if (index >= 2) {
			actualZ0 = actualZ;
		}

		if (index >= 5) {
			maxi++;
			mini++;
			if (finalZ[0] <= finalZ[1] && finalZ[1] >= finalZ[2]) {
				if (max == 0) {
					max = finalZ[1];
				} else if (finalZ[1] - min >= gate && waittingH == true
						&& maxi >= gate_f) {
					max = finalZ[1];
					waittingH = false;
					maxi = 0;
				} else if (finalZ[1] > max) {
					max = finalZ[1];
				}
			} else if (finalZ[0] >= finalZ[1] && finalZ[1] <= finalZ[2]) {
				if (min == 0) {
					min = finalZ[1];
				} else if (max - finalZ[1] >= gate && waittingH == false
						&& mini >= gate_f) {
					min = finalZ[1];
					waittingH = true;
					mini = 0;
					path++;
					pathAtOneTime++;
					if (!startAdd) {
						timeOfPathStart = timeOfTheDay;
					}
					startAdd = true;
					timeOfPathEnd = timeOfTheDay;
				} else if (finalZ[1] < min) {
					min = finalZ[1];
				}
			}
		}
	}

	public void dispActiveTime() {
		int second = (int) (activeTime / 100) % 60;
		int minute = (int) activeTime / 100 / 60;
		int hour = (int) activeTime / 100 / 60 / 60;
		textView3.setText("");
		if (hour < 10) {
			textView3.append("0");
		}
		textView3.append(String.valueOf(hour) + ":");
		if (minute < 10) {
			textView3.append("0");
		}
		textView3.append(String.valueOf(minute) + ":");
		if (second < 10) {
			textView3.append("0");
		}
		textView3.append(String.valueOf(second));

	}

	String[] encourage = { "���ܳ�����������û�еȳ����ĻԻ�", "�ɹ���·�ϲ���ӵ������Ϊ��ֵ��˲���",
			"�����ܸ��ӣ��ܲ��ܼ�", "��������Ӳ�����ܲ�Ҳ����������", "Ϊ�����ܣ�Ϊ���ֶ��ܣ�Ϊ��������",
			"�������彡����һ��ְ�𣬵���ֻ����������ʶ����һ��", "����Ȼ֮��������Ȼ֮��", "�ܲ���ʹ�˶�������!", "������ƽ��",
			"You have no idea what you are capable of until you try",
			"ϲ���ж����˺ܶ࣬�������ж����˲���", "��ʳ���ڽڣ�������ھ����������ں�", "�������ǿ׳���ܲ��ɣ�",
			"������뽡�����ܲ��ɣ�", "�������������ܲ��ɣ�", "��Ҫ�ɵø���", "����㲻�ܷɣ��Ǿͱ���",
			"��ǿ���������������������κ�һ���߷�", "����Լ�Խ�ݣ��������Խ����", "Your limit is only you!",
			"��չ�����˶�����ǿ��������", "Unless you puke,faint or die,keep going!",
			"�����Լ���������������", "��ѧ�Ļ����ǽ���������", "ֻҪ���ܣ�ÿ�춼��", "����ظ�����������",
			"Do a little more each day than you think you possibly can. ",
			"����Ҳ�ܲ�����һ���վ��ᵽ���������ǽ��죡", "��һ���ĳ۳�...", "�ɹ���·�ϲ���ӵ������Ϊ��ֵ��˲���",
			"��Ҫ����Ϊʲô�ܲ��������Լ�Ϊʲô����", "��������Ӳ�����ܲ�Ҳ����������", "", "" };

	public void dispPath() {
		textView1.setText(String.valueOf(path));

		if (path == 0) {
			textView11.setText("���컹û��ʼ�˶�Ŷ���Ͻ��������ɣ�");
			textView11.setTextColor(0xffff0000);
		} else if (path <= 20) {
			textView11.setText("ר�ҽ��������򲽣����Լ����Ŀ��ɣ�");
			textView11.setTextColor(0xffCDAD00);
		} else if (path <= 100) {
			textView11.setText("��Ҫ����Ϊʲô�ܲ��������Լ�Ϊʲô����");
			textView11.setTextColor(0xffD15FEE);
		} else if (path <= 12000) {
			textView11.setText(encourage[(int) (path / 400)]);
		} else if (path <= 20000) {
			textView11.setText("��ý�����������һ�����ǣ������ž�������~");
			textView11.setTextColor(0xff0000ff);
		} else if (path <= 30000) {
			textView11.setText("��˸�ĺ��վ����ҵ���!");
			textView11.setTextColor(0xffff0000);
		} else {
			textView11.setText("�й�����,��������!");
			textView11.setTextColor(0xffeeee00);
		}
		if (path == 10000) {// �����������˵�д��󣬿������������ģ�ɾ������
			Bitmap bmp = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.ic_launcher);
			NotificationManager manager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setLargeIcon(bmp)
					.setContentTitle("�ֻ��Ʋ�����")
					.setContentText("��ϲ�����ղ�������Ϊ�Լ��ĸ��ưɣ�")
					.setTicker("��һ���ĳ۳�...")
					.setDefaults(
							Notification.DEFAULT_LIGHTS
									| Notification.DEFAULT_SOUND);
			Notification notification = mBuilder.build();
			manager.notify(1, notification);
		}

	}

	public void dispCalorieAndDistance() {
		distance = (int) (path * pathLength);// cm
		calorie = (int) (1.036f * userWeight * distance / 100000);// cal
		fatCost = (int) (calorie / 7.7f);
		textView4.setText(calorie + "cal");
		textView5.setText((int) (distance / 100) + "m");
		textView10.setText("�� " + fatCost + "�˷���");
	}

	public void readDatabase() {
		// Log.i(TAG, "readDatabase");
		DatabaseHelper database = new DatabaseHelper(this);
		SQLiteDatabase myDatabase = null;
		myDatabase = database.getWritableDatabase();
		Cursor cursor = myDatabase.rawQuery(
				"select * from pathToday where dateToday=?",
				new String[] { dateToday });
		if (cursor.moveToFirst()) {
			path = cursor.getInt(cursor.getColumnIndex("path"));
			activeTime = cursor.getLong(cursor.getColumnIndex("activeTime"));
			activeTimeLast = activeTime;

		} else {
			// �µ����ڵ��ˣ���ʱ�����ɵ�д�룬��ǰ��������
			dateToday = c.get(Calendar.YEAR) + "��"
					+ (c.get(Calendar.MONTH) + 1) + "��" + mDay + "��";
			writeDatabase();
			dateToday = c.get(Calendar.YEAR) + "��"
					+ (c.get(Calendar.MONTH) + 1) + "��"
					+ c.get(Calendar.DAY_OF_MONTH) + "��";
			path = 0;
			activeTime = 0;
			timeOfTheDay = 0;
			timeOfPathStart = 0;
			timeOfPathEnd = 0;
			activeTimeLast = 0;
			calorie = 0;
			distance = 0;
			fatCost = 0;
		}

		cursor = myDatabase.query("userData", null, null, null, null, null,
				null);
		if (cursor.moveToFirst()) {
			userWeight = cursor.getFloat(cursor.getColumnIndex("userWeight"));
			pathLength = cursor.getFloat(cursor.getColumnIndex("pathLength"));
			sensorDegree = cursor.getFloat(cursor
					.getColumnIndex("sensorDegree"));
			gate = sensorDegree / 10;
			openBackgroundService = cursor.getInt(cursor
					.getColumnIndex("openBackgroundService"));
			// // Log.i("weight=", String.valueOf(userWeight));
			// Log.i("pathLength=", String.valueOf(pathLength));
			// Log.i("sensor=", String.valueOf(sensorDegree));
			// Log.i("openBackgroundService=",
			// String.valueOf(openBackgroundService));
		}

		cursor.close();
		myDatabase.close();
	}

	public void writeDatabase() {
		// Log.i(TAG, "writeDatabase");
		DatabaseHelper database = new DatabaseHelper(this);
		SQLiteDatabase myDatabase = null;
		myDatabase = database.getWritableDatabase();
		Cursor cursor = myDatabase.rawQuery(
				"select * from pathToday where dateToday=?",
				new String[] { dateToday });
		if (cursor.moveToFirst()) {
			String sql = "update [pathToday] set path = ?, activeTime =?, "
					+ "calorie =?, fatCost =?, distance =?, "
					+ "userWeight=?, pathLength=? where dateToday=? ";// �޸ĵ�SQL���
			myDatabase.execSQL(
					sql,
					new String[] { String.valueOf(path),
							String.valueOf(activeTime),
							String.valueOf(calorie), String.valueOf(fatCost),
							String.valueOf(distance),
							String.valueOf(userWeight),
							String.valueOf(pathLength), dateToday });// ִ���޸�,�������ݴ洢

		} else {
			String sql = "insert into pathToday(dateToday, path, activeTime, calorie, fatCost, distance, userWeight, pathLength) values(?,?,?,?,?,?,?,?);";
			myDatabase.execSQL(
					sql,
					new String[] { dateToday, String.valueOf(path),
							String.valueOf(activeTime),
							String.valueOf(calorie), String.valueOf(fatCost),
							String.valueOf(distance),
							String.valueOf(userWeight),
							String.valueOf(pathLength) });// �������ݲ��洢

		}
		cursor.close();
		myDatabase.close();
	}

	// ���ݿ�----������ʷ��¼���Լ�activity��service���л�ʱ�����ݴ洢�����
	public class DatabaseHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "mydata.db"; // ���ݿ�����
		private static final int version = 1; // ���ݿ�汾

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Log.i(TAG, "CreatDatabase");
			String sql = "create table pathToday("
					+ "dateToday varchar(20) primary key, " + "path INTEGER,"
					+ "activeTime INTEGER," + "calorie INTEGER,"
					+ "fatCost INTEGER," + "distance INTEGER,"
					+ "userWeight float," + "pathLength float,"
					+ "openBackgroundService INTEGE);";
			db.execSQL(sql);
			sql = "create table userData(" + "pathLength float,"
					+ "userWeight float," + "sensorDegree float,"
					+ "openBackgroundService INTEGE);";
			db.execSQL(sql);
			sql = "insert into userData(pathLength, userWeight, sensorDegree, openBackgroundService) values(?,?,?,?);";
			db.execSQL(
					sql,
					new String[] { String.valueOf(pathLength),
							String.valueOf(userWeight),
							String.valueOf(sensorDegree),
							String.valueOf(openBackgroundService) });// �������ݲ��洢

			// sql =
			// "insert into pathToday(dateToday, path, activeTime, carlorie, fatCost, distance, userWeight,pathLength) values(?, 10,20,30,40,50,60,70);";
			// db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

	class AsyncMain extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			long timeNow = timeOfTheDay;
			while (timeOfTheDay - timeNow <= 10) { // �ӳ�100ms���ȴ�service����
			}
			readDatabase();
			publishProgress();
			while (!cancelThread) {
				if (startSensor) {

					if (timeOfTheDay - timeNow >= 1) { // 100Hz
						timeNow = timeOfTheDay;

						strideCalculation(values0, values1, values2);
						if (timeOfTheDay - timeOfPathEnd > DELAY_TIME
								&& startAdd) {
							startAdd = false;
							activeTimeLast = activeTime;

							if (pathAtOneTime <= DELAY_NUM) {
								if (path - DELAY_NUM >= 0) {
									path = path - DELAY_NUM;
									activeTime = activeTime - 200;
									activeTimeLast = activeTimeLast - 200;
									publishProgress();
								}
							}
							pathAtOneTime = 0;
						}
						if (startAdd) {
							activeTime = (timeOfTheDay - timeOfPathStart)
									+ activeTimeLast;
							publishProgress();

						}

					}
				}
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			// Log.i("activity�첽�̣߳�", "����");
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			// Log.i("activity�첽�̣߳�", "��ʼ");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			dispActiveTime();
			dispPath();
			dispCalorieAndDistance();
			if (openBackgroundService != 0 && firstCreat) {
				textView12.setText("�Ʋ���");
				sensorManager.registerListener(sensorEventListener, sensor,
						SensorManager.SENSOR_DELAY_FASTEST);// ע�ᴫ����������
				firstCreat = false;
				startSensor = true;
				progressBar.setVisibility(View.VISIBLE);
				startButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
				continueButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.INVISIBLE);
			}
			if (openBackgroundService == 0 && firstCreat) {
				textView12.setText("δ��ʼ");
				firstCreat = false;
				startSensor = false;
				progressBar.setVisibility(View.INVISIBLE);
				startButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.INVISIBLE);
				continueButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.INVISIBLE);
				sensorManager.unregisterListener(sensorEventListener, sensor);// ȡ������
			}
			if (firstStart) {
				firstStart = false;
				if (openBackgroundService != 2) {
					lockScreenButton.setVisibility(View.INVISIBLE);
				}

				switch (openBackgroundService) {
				case 1:// ��������ģʽ
						// ʲô����������Onpause��ʱ����жϣ�Ȼ����service������
						// Log.i(TAG, "��������ģʽ");
					break;
				case 2:// ����ģʽ
					acquireWakeLock(); // ��ȡ��Դ��
					// Log.i(TAG, "��������ģʽ");
					Toast.makeText(MainActivity.this, "���������ť�����������ģʽ",
							Toast.LENGTH_SHORT).show();
					lockScreenButton.setVisibility(View.VISIBLE);

					break;
				case 3:// �ڵ�ģʽһ
					acquireWakeLock2();// ��ȡ��Դ��
					// Log.i(TAG, "�ڵ�ģʽһ");
					break;
				case 4:// �ڵ�ģʽ��
					acquireWakeLock2();// ��ȡ��Դ��
					// Log.i(TAG, "�ڵ�ģʽ��");
					break;

				default:
					break;
				}
			}

			super.onProgressUpdate(values);
		}

	}

	// ����Ū���������ˣ����������ֲ�����Ĳ˵��ǣ���һ��

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		if (menuDialog == null) {
			menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		} else {
			menuDialog.show();
		}
		return false;// ����Ϊtrue ����ʾϵͳmenu
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}

	// �ѺͲ˵���صĴ������������������

	public void creatMenu() {

		menuView = View.inflate(this, R.layout.my_menu, null);
		// ����AlertDialog
		menuDialog = new AlertDialog.Builder(this).create();
		// //����͸����
		// Window window = menuDialog.getWindow();
		// WindowManager.LayoutParams lp = window.getAttributes();
		// lp.alpha = 0.7f;
		// window.setAttributes(lp);

		menuDialog.setView(menuView);
		menuDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)// ��������
					dialog.dismiss();
				return false;
			}
		});

		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		/** ����menuѡ�� **/
		menuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				menuDialog.hide();
				switch (arg2) {
				case 0:// ��ʷ
					openBackgroundService = 0;
					Intent intent = new Intent(MainActivity.this,
							HistoryActivity.class);
					startActivity(intent);
					break;
				case 1:// ע������

					Intent intent1 = new Intent(MainActivity.this,
							NoteActivity.class);
					startActivity(intent1);
					break;
				case 2:// ����
					openBackgroundService = 0;
					Intent intent11 = new Intent(MainActivity.this,
							SettingActivity.class);
					startActivity(intent11);
					break;
				case 3:// �Ʋ�ԭ��
					Intent intent3 = new Intent(MainActivity.this,
							TheoryActivity.class);
					startActivity(intent3);
					break;
				case 4:// ��ϸ
					Intent intent4 = new Intent(MainActivity.this,
							AboutActivity.class);
					startActivity(intent4);
					break;
				case 5:// ����
					appwall.doShowAppWall();
					// Log.i(TAG, "wall");
					break;
				}

			}
		});

		imageButtonMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!lockScreen) {

					if (menuDialog == null) {
						menuDialog = new AlertDialog.Builder(MainActivity.this)
								.setView(menuView).show();
					} else {
						menuDialog.show();
					}
				}
			}
		});

		imageButtonNaruto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!lockScreen) {

					openBackgroundService = 0;
					Intent intent5 = new Intent(MainActivity.this,
							HistoryActivity.class);
					startActivity(intent5);
				}
			}
		});

	}

	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.my_menu_items,
				new String[] { "itemImage", "itemText" }, new int[] {
						R.id.item_image, R.id.item_text });
		return simperAdapter;
	}

	// �����豸��Դ��
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
					TAG);
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	private void acquireWakeLock2() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	// �ͷ��豸��Դ��
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

}
