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
package com.tencent.wstt.gt.datasource.engine;

import com.tencent.wstt.gt.datasource.util.SFUtils;

import java.lang.reflect.Field;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流畅度数据采集引擎
 */
public class SMTimerTask extends TimerTask {
	private boolean hasSu = true;
	private int standardSM = 60; // 标准的SM值，如果是未来兼容VR设备的手机，也可能达到120或更高
	private boolean isGetPeriod = false; // 是否已获取执行间隔值
	private long thisPeriod = 1000; // 默认1000ms，在初次执行时要通过反射方法在超类中获取真实值

	private int pid;
	private DataRefreshListener<Long> dataRefreshListener;

	private BlockingQueue<Integer> queue;
	private AtomicInteger count = new AtomicInteger(0);
	private boolean pause = false;

	/**
	 * 构造方法
	 * @param pid 指定进程号
	 * @param dataRefreshListener 数据的监听器
	 * @param hasSu 是否已root,事实上获取流畅度数值必须root
	 */
	public SMTimerTask(int pid, DataRefreshListener<Long> dataRefreshListener, boolean hasSu)
	{
		this.pid = pid;
		this.dataRefreshListener = dataRefreshListener;
		this.hasSu = hasSu;
		queue = SFUtils.startSampleSF(pid);
		Thread dataCountThread = new Thread("GTSMRunner") {
			@Override
			public void run() {
				while (!pause)
				{
					try {
						int value = queue.take();
						count.addAndGet(value);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		};
		dataCountThread.start();
	}

	public boolean isHasSu() {
		return hasSu;
	}

	public void setHasSu(boolean hasSu) {
		this.hasSu = hasSu;
	}

	public int getStandardSM() {
		return standardSM;
	}

	/**
	 * 设置标准SM值
	 * @param standardSM 必须大于0，否则默认设置为60
	 */
	public void setStandardSM(int standardSM) {
		this.standardSM = standardSM <= 0 ? 60 : standardSM;
	}
	/*
	 * 主循环，1s执行一次
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		/*
		 * Timer的启动无法避免，所以这里加root保护
		 * 若未root，直接返回，减少空消耗
		 */
		if (! hasSu)
		{
			return;
		}

		// Task的执行采样间隔
		if (! isGetPeriod)
		{
			Class<?> clz = TimerTask.class;
			Field superPeriod = null;
			try {
				superPeriod = clz.getDeclaredField("period");
				superPeriod.setAccessible(true);
				thisPeriod = superPeriod.getLong(this);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			isGetPeriod = true;
		}
		long tempStandardSM = standardSM * thisPeriod / 1000; // 比如standardSM=60，scheduled=500，tempStandardSM的值就是30

		int x = count.getAndSet(0);
		// 卡顿大于60时，要将之前几次SM计数做修正
		if (x > tempStandardSM) {
			long n = x / tempStandardSM;
			long v = x % tempStandardSM;

			for (int i = 0; i < n; i++) {
				dataRefreshListener.onRefresh(System.currentTimeMillis() - thisPeriod * i , Long.valueOf(0));
			}
			dataRefreshListener.onRefresh(System.currentTimeMillis(), Long.valueOf(60 - v * 1000 / thisPeriod));
		} else {
			dataRefreshListener.onRefresh(System.currentTimeMillis(), Long.valueOf(60 - x * 1000 / thisPeriod));
		}
	}

	public void stop()
	{
		pause = true;
		this.cancel();
		SFUtils.stopSampleSF(pid);
	}
}