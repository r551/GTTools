/*
 * Tencent is pleased to support the open source community by making
 * Tencent GT (Version 2.4 and subsequent versions) available.
 *
 * Notwithstanding anything to the contrary herein, any previous version
 * of Tencent GT shall not be subject to the license hereunder.
 * All right, title, and interest, including all intellectual property rights,
 * in and to the previous version of Tencent GT (including any and all copies thereof)
 * shall be owned and retained by Tencent and subject to the license under the
 * Tencent GT End User License Agreement (http://gt.qq.com/wp-content/EULA_EN.html).
 * 
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 * 
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.wstt.gt.datasource.util;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 获取本进程的流畅度信息
 */
public class SMUtils {
	private static boolean running;
	private static AtomicInteger i = new AtomicInteger(0);

	private static Choreographer.FrameCallback c = new Choreographer.FrameCallback()
	{
		@Override
		public void doFrame(long frameTimeNanos)
		{
			if (!running) {
				return;
			}
			// 本工具类中只负责帧数的递增计数，需要在调用端控制计数器的清零时机
			i.incrementAndGet();

			Choreographer.getInstance().postFrameCallback(this);
		}
	};

	/**
	 * 启动流畅度的采集
	 * @return 核心计数器
	 */
	public static AtomicInteger startSampleSM()
	{
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			@Override
			public void run() {
				running = true;
				Choreographer.getInstance().postFrameCallback(c);
			}
		});
		return i;
	}

	public static void stopSampleSM()
	{
		running = false;
		i.set(0);
	}

	public static boolean isRunning()
	{
		return running;
	}

	/**
	 * 基于标准流畅度为60的算分方法
	 * @param lst 计算数据源
	 * @return 计算结果列表
	 */
	public static int[] getSmDetail(List<Long> lst) {
		int[] resultList = new int[6];
		if (lst == null || lst.size() == 0)
			return resultList;

		double delta = 1.2;
		double w = 0.4;
		int s = 5;
		int count5 = 0;
		long minsm = 60;
		int ktimes = 0;
		int high = 0;
		int highScore = 0;
		int lowScore = 0;
		int low = 0;
		int total = 0;
		int count = 0;
		double resultn = 0;
		double result = 0;
		long lastdata = -1;
		double sscore = 0;
		double kscore = 0;

		ArrayList<Long> tempDataList = new ArrayList<Long>();

		long sm = 0;

		for (int i = 0; i < lst.size(); i++) {

			count5 += 1;
			try {
				sm = lst.get(i);
			} catch (Exception e) {

			}
			minsm = (minsm > sm) ? sm : minsm;

			if (sm < 40) {
				ktimes += 1;
			}
			if (count5 == s) {
				if (minsm >= 40) {
					high += 1;
				} else {
					low += 1;
					minsm *= Math.pow(delta, 1.0 / ktimes - 1);
				}
				total += 1;
				tempDataList.add(minsm);
				minsm = 60;
				count5 = 0;
				ktimes = 0;
			}
		}
		if (count5 > 0) {
			if (minsm >= 40)
				high += 1;
			else {
				low += 1;
				minsm *= Math.pow(delta, 1.0 / ktimes - 1);
			}
			total += 1;

			tempDataList.add(minsm);
		}
		resultList[0] = low / total;
		count = 0;
		resultn = 0;
		result = 0;
		lastdata = -1;
		sscore = 0;
		kscore = 0;

		for (int i = 0; i < tempDataList.size(); i++) {
			Long data = tempDataList.get(i);

			if (lastdata < 0) {
				lastdata = data;
			}
			if (data >= 40) {
				if (lastdata < 40) {
					kscore += resultn;
					result += resultn;
					count = 0;
					resultn = 0;
				}
				resultn += getScore(data);

				count += 1;
			} else {
				if (lastdata >= 40) {
					result += resultn * w;
					sscore += resultn;
					count = 0;
					resultn = 0;
				}
				count += 1;
				resultn += getScore(data);

			}
			lastdata = data;

		}

		if (count > 0 && lastdata < 40) {
			result += resultn;
			kscore += resultn;
		}

		if (count > 0 && lastdata >= 40) {
			result += resultn * w;
			sscore += resultn;
		}

		if (low > 0) {
			lowScore = (int) (kscore * 100 / low);
		}
		if (high > 0) {
			highScore = (int) (sscore * 100 / high);
		}

		resultList[1]= low * 5;
		resultList[2] = lowScore;
		resultList[3] = high * 5;
		resultList[4] = highScore;
		resultList[5] = (int) (result * 100 / (high * w + low));
		return resultList;

	}

	private static double getScore(Long data) {
		if (data < 20) {
			return data * 1.5 / 100.0;
		}

		else if (data < 30 && data >= 20) {
			return 0.3 + (data - 20) * 3 / 100.0;
		}

		else if (data < 50 && data >= 30) {
			return 0.6 + (data - 30) / 100.0;
		} else {
			return 0.8 + (data - 50) * 2 / 100.0;
		}
	}

}
