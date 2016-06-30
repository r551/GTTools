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

import com.tencent.wstt.gt.datasource.util.CPUUtils;
import com.tencent.wstt.gt.datasource.util.ProcessCPUUtils;

import java.util.TimerTask;

/**
 * CPU数据采集引擎，以定时任务的方式实现，因为CPU采集算法中会停滞指定的时间，所以该任务在定时器的时间间隔要设置为1ms。
 * 支持整机CPU的采集和指定进程CPU的采集。
 */
public class CPUTimerTask extends TimerTask {
	private int pid;
	private int interval;
	private DataRefreshListener<Double> cpuFreshListener;
	private DataRefreshListener<Long> jiffiesFreshListener;

	/**
	 * 构造方法
	 * @param pid 指定进程号，0或负数为采集整机的CPU数据
	 * @param interval 采样间隔
	 * @param cpuFreshListener CPU数据的回调
	 * @param jiffiesFreshListener CPU时间片数据回调，只在采集指定进程的数据时有效
	 */
	public CPUTimerTask(int pid, int interval,
			DataRefreshListener<Double> cpuFreshListener, DataRefreshListener<Long> jiffiesFreshListener)
	{
		this.pid = pid;
		this.interval = interval;
		this.cpuFreshListener = cpuFreshListener;
		this.jiffiesFreshListener = jiffiesFreshListener;
	}

	public void run() {
		if (pid > 0) // 进程的
		{
			double result[] = ProcessCPUUtils.getUsage(pid, interval);
			cpuFreshListener.onRefresh(System.currentTimeMillis(), Double.valueOf(result[0]));
			jiffiesFreshListener.onRefresh(System.currentTimeMillis(), Long.valueOf((long)result[1]));
		}
		else // 整机的
		{
			double result = CPUUtils.getUsage(interval);
			cpuFreshListener.onRefresh(System.currentTimeMillis(), Double.valueOf(result));
		}
	}

	public void stop()
	{
		this.cancel();
	}
}