package com.jqd.stridecalculation.model;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-27 ����3:02:22
 * @description �Ʋ��㷨�����Ĳ���
 */
public class StepCount {
	public int pathAtOneTime = 0; // �����Ĳ�����
	public long timeOfTheDay = 0;
	public long timeOfPathStart = 0;
	public long timeOfPathEnd = 0;
	public boolean startAdd = false;
	private float x = 0;
	private float y = 0;
	private float z = 0;
	private float actualZ0 = 0f;
	private float actualZ;
	private float[] finalZ = { 0f, 0f, 0f };
	private float value0, value1, value2;
	public float gate = 4.0f;
	public float gate_f = 15.0f;
	private float max = 0;
	private float min = 0;
	private int maxi = 0;
	private int mini = 0;
	private boolean waittingH = true;
	public int path = 0;
	private int index = 1;

	private volatile static StepCount stepCount = null;
	public static StepCount getInstance() {
		if (stepCount == null) {
			synchronized (StepCount.class) {
				if (stepCount == null) {
					stepCount = new StepCount();
				}
			}
		}
		return stepCount;
	}
	
	/**
	 * @�㷨˼·��
		%1. ��ͨ�˲���
		%2. Ѱ�Ҳ��岨�ȡ�����һ�鲨�岨�ȡ�ʹ������������һ��
		%       ��������ʱ�̵ļ��ٶȣ��ж��м�ʱ���Ƿ�Ϊ��ֵ��
		%           �����Ǽ���ֵ���������һ������Сֵ�������ֵ�������㲽Ƶ���������ڵȴ�������֣���ô�����жϳɹ���
		%           �����Ǽ�Сֵ���������һ�������ֵ�������ֵ�������㲽Ƶ���������ڵȴ����ȳ��֣���ô�����жϳɹ���������һ��
	 */
	public void strideCalculation(float data1, float data2, float data3) {
		if (index <= 10) {
			index++;
		}
		if (index >= 2) { //�˲�
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

	
}
