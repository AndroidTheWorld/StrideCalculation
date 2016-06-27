package com.jqd.stridecalculation.data;

import java.util.Calendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jqd.stridecalculation.model.StepCount;
import com.jqd.stridecalculation.task.AsyncMainTask;
import com.jqd.stridecalculation.ui.MainActivity;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����5:05:51
 * @description ���ݿ�Ĳ�����أ�ʹ��SQLite
 */
public class DataManager {
	public float userWeight = 50f;// kg
	public float pathLength = 60.0f;// cm
	public int calorie = 0;
	public int distance = 0;
	public int fatCost = 0;
	public String dateToday;
	public int mDay;
	public Calendar c;
	public float sensorDegree = 40;
	
	private volatile static DataManager dataManager = null;
	public static DataManager getInstance() {
		if (dataManager == null) {
			synchronized (DataManager.class) {
				if (dataManager == null) {
					dataManager = new DataManager();
				}
			}
		}
		return dataManager;
	}
	
	public void readDatabase(MainActivity activity, AsyncMainTask asyncMainTask) {
		// Log.i(TAG, "readDatabase");
		DatabaseHelper database = new DatabaseHelper(activity);
		SQLiteDatabase myDatabase = null;
		myDatabase = database.getWritableDatabase();
		Cursor cursor = myDatabase.rawQuery(
				"select * from pathToday where dateToday=?",
				new String[] { dateToday });
		StepCount stepCount = StepCount.getInstance();
		if (cursor.moveToFirst()) {
			stepCount.path = cursor.getInt(cursor.getColumnIndex("path"));
			asyncMainTask.activeTime = cursor.getLong(cursor
					.getColumnIndex("activeTime"));
			asyncMainTask.activeTimeLast = asyncMainTask.activeTime;

		} else {
			// �µ����ڵ��ˣ���ʱ�����ɵ�д�룬��ǰ��������
			dateToday = c.get(Calendar.YEAR) + "��"
					+ (c.get(Calendar.MONTH) + 1) + "��" + mDay + "��";
			writeDatabase(activity, asyncMainTask);
			dateToday = c.get(Calendar.YEAR) + "��"
					+ (c.get(Calendar.MONTH) + 1) + "��"
					+ c.get(Calendar.DAY_OF_MONTH) + "��";
			stepCount.path = 0;
			asyncMainTask.activeTime = 0;
			stepCount.timeOfTheDay = 0;
			stepCount.timeOfPathStart = 0;
			stepCount.timeOfPathEnd = 0;
			asyncMainTask.activeTimeLast = 0;
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
			stepCount.gate = sensorDegree / 10;
			activity.openBackgroundService = cursor.getInt(cursor
					.getColumnIndex("openBackgroundService"));
		}

		cursor.close();
		myDatabase.close();
	}

	public void writeDatabase(MainActivity activity, AsyncMainTask asyncMainTask) {
		// Log.i(TAG, "writeDatabase");
		DatabaseHelper database = new DatabaseHelper(activity);
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
					new String[] { String.valueOf(StepCount.getInstance().path),
							String.valueOf(asyncMainTask.activeTime),
							String.valueOf(calorie), String.valueOf(fatCost),
							String.valueOf(distance),
							String.valueOf(userWeight),
							String.valueOf(pathLength), dateToday });// ִ���޸�,�������ݴ洢

		} else {
			String sql = "insert into pathToday(dateToday, path, activeTime, calorie, fatCost, distance, userWeight, pathLength) values(?,?,?,?,?,?,?,?);";
			myDatabase.execSQL(
					sql,
					new String[] { dateToday, String.valueOf(StepCount.getInstance().path),
							String.valueOf(asyncMainTask.activeTime),
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
		private MainActivity activity;

		public DatabaseHelper(MainActivity activity) {
			super(activity, DB_NAME, null, version);
			this.activity = activity;
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
							String.valueOf(activity.openBackgroundService) });// �������ݲ��洢
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

}
