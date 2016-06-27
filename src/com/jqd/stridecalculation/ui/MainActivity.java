package com.jqd.stridecalculation.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import com.jqd.stridecalculation.ad.AppWallManager;
import com.jqd.stridecalculation.data.DataManager;
import com.jqd.stridecalculation.model.SensorsModel;
import com.jqd.stridecalculation.model.StepCount;
import com.jqd.stridecalculation.task.AsyncMainTask;
import com.jqd.stridecalculation.task.BackgroundService;
import com.tencent.stat.StatService;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����5:09:13
 * @description ������
 */
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

	private DataManager dataManager;
	private SensorsModel sensorsModel;

	private float bright_careful = 0.005f;
	public boolean lockScreen = false;
	private Thread timeThread;
	private int barStart = 100;
	PowerManager.WakeLock mWakeLock = null;
	public int openBackgroundService = 0;
	String TAG = "MainActivity";
	private AsyncMainTask asyncMainTask;
	private boolean firstStart = true;
	private boolean firstCreat = true;
	private float screenBrightnessLock = 0.01f;
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private SharedPreferences settings;
	private boolean isNewUser = true;
	private AlertDialog forNewUsersDialog;
	private AppWallManager appWallManager;
	private StepCount stepCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,
				FLAG_HOMEKEY_DISPATCHED);// ��������home��
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);// �Զ��岼�ָ�ֵ
		StatService.trackCustomEvent(this, "onCreate", "");

		initResources();
		initalShow();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		// �洢���ݣ�������������Ʋ�,�ر��̣߳�ȡ������������
		dataManager.writeDatabase(MainActivity.this, asyncMainTask);
		if (openBackgroundService != 0) {
			Intent intent = new Intent(MainActivity.this,
					BackgroundService.class);
			startService(intent);
		}
		asyncMainTask.cancelThread = true;
		releaseWakeLock();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// ��ȡ����
		dataManager.dateToday = dataManager.c.get(Calendar.YEAR) + "��"
				+ (dataManager.c.get(Calendar.MONTH) + 1) + "��"
				+ dataManager.c.get(Calendar.DAY_OF_MONTH) + "��";
		textView15.setText(dataManager.dateToday);
		firstStart = true;
		// ��ʼ�첽�ļ�ʱ��
		asyncMainTask.cancelThread = false;
		// Log.i(TAG, "MainActivity Resume!");
		timeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!asyncMainTask.cancelThread) {
						Thread.sleep(10);// ˯10ms
						stepCount.timeOfTheDay++;
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
		asyncMainTask = new AsyncMainTask(MainActivity.this);
		asyncMainTask.execute();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
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
					asyncMainTask.startSensor = true;
					progressBar.setVisibility(View.VISIBLE);
					startButton.setVisibility(View.INVISIBLE);
					pauseButton.setVisibility(View.VISIBLE);

					sensorsModel.sensorManager.registerListener(
							sensorEventListener, sensorsModel.sensor,
							SensorManager.SENSOR_DELAY_FASTEST);
					break;
				case R.id.pauseButton:
					textView12.setText("��ͣ��");
					asyncMainTask.startSensor = false;
					stepCount.startAdd = false;
					progressBar.setVisibility(View.INVISIBLE);
					pauseButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.VISIBLE);
					sensorsModel.sensorManager.unregisterListener(
							sensorEventListener, sensorsModel.sensor);// ȡ������
					break;
				case R.id.continueButton:
					textView12.setText("�Ʋ���");
					asyncMainTask.startSensor = true;
					progressBar.setVisibility(View.VISIBLE);
					pauseButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.INVISIBLE);
					sensorsModel.sensorManager.registerListener(
							sensorEventListener, sensorsModel.sensor,
							SensorManager.SENSOR_DELAY_FASTEST);
					break;
				case R.id.stopButton:
					textView12.setText("δ��ʼ");
					asyncMainTask.startSensor = false;
					stepCount.startAdd = false;
					progressBar.setVisibility(View.INVISIBLE);
					startButton.setVisibility(View.VISIBLE);
					stopButton.setVisibility(View.INVISIBLE);
					continueButton.setVisibility(View.INVISIBLE);
					sensorsModel.sensorManager.unregisterListener(
							sensorEventListener, sensorsModel.sensor);// ȡ������
					stepCount.path = 0;
					stepCount.pathAtOneTime = 0;
					asyncMainTask.activeTime = 0;
					asyncMainTask.activeTimeLast = 0;
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

	public void dispActiveTime() {
		int second = (int) (asyncMainTask.activeTime / 100) % 60;
		int minute = (int) asyncMainTask.activeTime / 100 / 60;
		int hour = (int) asyncMainTask.activeTime / 100 / 60 / 60;
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

	public void dispPath() {
		textView1.setText(String.valueOf(stepCount.path));
		if (stepCount.path == 10000) {// �����������˵�д��󣬿������������ģ�ɾ������
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
		dataManager.distance = (int) (stepCount.path * dataManager.pathLength);// cm
		dataManager.calorie = (int) (1.036f * dataManager.userWeight
				* dataManager.distance / 100000);// cal
		dataManager.fatCost = (int) (dataManager.calorie / 7.7f);
		textView4.setText(dataManager.calorie + "cal");
		textView5.setText((int) (dataManager.distance / 100) + "m");
		textView10.setText("�� " + dataManager.fatCost + "�˷���");
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
					appWallManager.showAppWall();
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

	private void initResources() {
		appWallManager = new AppWallManager(MainActivity.this);
		stepCount = StepCount.getInstance();
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
	}

	private class lockScreenOnClickListener implements OnClickListener {
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
	}

	SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			asyncMainTask.values0 = event.values[0];
			asyncMainTask.values1 = event.values[1];
			asyncMainTask.values2 = event.values[2];
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private class seekBarListener implements OnSeekBarChangeListener {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
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
	}

	private void showForNuwUser() {
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

	public void showNoSensorsInfo() {
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
				appWallManager.showAppWall();
			}
		});

		builder.setView(dialogSettingGroup);
		forNewUsersDialog = builder.create();
		forNewUsersDialog.show();
	}

	private void initalShow() {
		// ����󶨼�����
		startButton.setOnClickListener(clickListener);
		stopButton.setOnClickListener(clickListener);
		pauseButton.setOnClickListener(clickListener);
		continueButton.setOnClickListener(clickListener);
		// ģ������
		lockScreenButton.setOnClickListener(new lockScreenOnClickListener());
		seekBar.setOnSeekBarChangeListener(new seekBarListener());
		if (isNewUser) { // ����ǵ�һ��ʹ�����������˵���Ի���
			showForNuwUser();
		}

		dataManager = DataManager.getInstance();
		dataManager.c = Calendar.getInstance();
		dataManager.mDay = dataManager.c.get(Calendar.DAY_OF_MONTH);
		asyncMainTask = new AsyncMainTask(MainActivity.this);
		// ������
		sensorsModel = new SensorsModel();
		sensorsModel.sensorInit(MainActivity.this, asyncMainTask);
	}

	public void updateViews() {
		dispActiveTime();
		dispPath();
		dispCalorieAndDistance();
		if (openBackgroundService != 0 && firstCreat) {
			textView12.setText("�Ʋ���");
			sensorsModel.sensorManager.registerListener(sensorEventListener,
					sensorsModel.sensor, SensorManager.SENSOR_DELAY_FASTEST);
			firstCreat = false;
			asyncMainTask.startSensor = true;
			progressBar.setVisibility(View.VISIBLE);
			startButton.setVisibility(View.INVISIBLE);
			pauseButton.setVisibility(View.VISIBLE);
			continueButton.setVisibility(View.INVISIBLE);
			stopButton.setVisibility(View.INVISIBLE);
		}
		if (openBackgroundService == 0 && firstCreat) {
			textView12.setText("δ��ʼ");
			firstCreat = false;
			asyncMainTask.startSensor = false;
			progressBar.setVisibility(View.INVISIBLE);
			startButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.INVISIBLE);
			continueButton.setVisibility(View.INVISIBLE);
			stopButton.setVisibility(View.INVISIBLE);
			sensorsModel.sensorManager.unregisterListener(sensorEventListener,
					sensorsModel.sensor);// ȡ������
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
	}
}
