package com.jqd.stridecalculation.task;

import android.os.AsyncTask;

import com.jqd.stridecalculation.data.DataManager;
import com.jqd.stridecalculation.model.StepCount;
import com.jqd.stridecalculation.ui.MainActivity;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����5:07:13
 * @description �������ϵĺ�̨�첽task������ȥ�����ٶȼƲ�������
 */
public class AsyncMainTask extends AsyncTask<Void, Void, Void> {
	public boolean cancelThread = false;
	public boolean startSensor = false;
	public long activeTime = 0;
	public long activeTimeLast = 0;
	public int DELAY_NUM = 1;// ����⵽���ǵ�����һ��������Ϊ�ǶԵģ������ټ�ȥ
	private int DELAY_TIME = 200; // 2sû�м�⵽�����Զ��رռ�ʱ��
	public float values0 = 0;
	public float values1 = 0;
	public float values2 = 0;
	private MainActivity activity;
	
	protected Void doInBackground(Void... params) {
		StepCount stepCount = StepCount.getInstance();
		long timeNow = stepCount.timeOfTheDay;
		while (stepCount.timeOfTheDay - timeNow <= 10) { // �ӳ�100ms���ȴ�service����
		}
		DataManager.getInstance().readDatabase(activity, AsyncMainTask.this);
		publishProgress();
		while (!cancelThread) {
			if (startSensor) {

				if (stepCount.timeOfTheDay - timeNow >= 1) { // 100Hz
					timeNow = stepCount.timeOfTheDay;

					stepCount.strideCalculation(values0, values1, values2);
					if (stepCount.timeOfTheDay - stepCount.timeOfPathEnd > DELAY_TIME
							&& stepCount.startAdd) {
						stepCount.startAdd = false;
						activeTimeLast = activeTime;

						if (stepCount.pathAtOneTime <= DELAY_NUM) {
							if (stepCount.path - DELAY_NUM >= 0) {
								stepCount.path = stepCount.path - DELAY_NUM;
								activeTime = activeTime - 200;
								activeTimeLast = activeTimeLast - 200;
								publishProgress();
							}
						}
						stepCount.pathAtOneTime = 0;
					}
					if (stepCount.startAdd) {
						activeTime = (stepCount.timeOfTheDay - stepCount.timeOfPathStart)
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
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	public AsyncMainTask(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		activity.updateViews();
		super.onProgressUpdate(values);
	}

}
