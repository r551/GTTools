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
package com.tencent.wstt.gt.datasource;

import com.tencent.wstt.gt.datasource.engine.CPUTimerTask;
import com.tencent.wstt.gt.datasource.engine.DataRefreshListener;

import org.junit.Test;

import java.util.Timer;

public class CPUTest {
	@Test
	public void testCPU() throws InterruptedException
	{
		Timer timer = new Timer();

		// 进程号填0，关注的即整机的CPU
		CPUTimerTask task = new CPUTimerTask(0, 1000,
			new DataRefreshListener<Double>(){
	
				@Override
				public void onRefresh(long time, Double data) {
					System.out.println("CPU:" + data);
				}},null);

		timer.schedule(task, 0, 1);
		Thread.sleep(10000);
	}

	@Test
	public void testProcessCPU() throws InterruptedException
	{
		Timer timer = new Timer();

		// 测试代码中以自身进程数据
		CPUTimerTask task = new CPUTimerTask(android.os.Process.myPid(), 1000,
				new DataRefreshListener<Double>(){

					@Override
					public void onRefresh(long time, Double data) {
						System.out.println("CPU:" + data);
					}},

				new DataRefreshListener<Long>(){

					@Override
					public void onRefresh(long time, Long data) {
						System.out.println("Jiffies:" + data);
					}});

		timer.schedule(task, 0, 1);
		Thread.sleep(10000);
	}
}
