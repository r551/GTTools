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

import com.tencent.wstt.gt.datasource.engine.DataRefreshListener;
import com.tencent.wstt.gt.datasource.engine.NETTimerTask;
import com.tencent.wstt.gt.datasource.engine.UidNETTimerTask;
import com.tencent.wstt.gt.datasource.util.UidNETUtils;

import org.junit.Test;

import java.util.Timer;

public class NETTest {
	@Test
	// 需要权限：android.permission.ACCESS_NETWORK_STATE
	public void testNet() throws InterruptedException
	{
		Timer timer = new Timer();
		
		NETTimerTask task = new NETTimerTask(
			new DataRefreshListener<Double[]>(){
	
				@Override
				public void onRefresh(long time, Double[] data) {
					System.out.println(data[0] + "/" + data[1] + "/" + data[2]+ "/" + data[3]+ "/" + data[4]+ "/" + data[5]);
				}});

		timer.schedule(task, 0, 1000);
		Thread.sleep(10000);
	}

	@Test
	public void testUidNet() throws InterruptedException
	{
		Timer timer = new Timer();
		// 需要先选择适合自己环境的采集方案
		if (!UidNETUtils.test(-1))
		{
			UidNETUtils.Case c = new UidNETUtils.CaseTrafficStats();
			if (!c.test(-1)) c = new UidNETUtils.CaseInvalid();
			UidNETUtils.setSampleCase(c);
		}

		UidNETTimerTask task = new UidNETTimerTask(1000,
			new DataRefreshListener<Double[]>(){
	
				@Override
				public void onRefresh(long time, Double[] data) {
					System.out.println(data[0] + "/" + data[1]);
				}});

		timer.schedule(task, 0, 1000);
		Thread.sleep(100000);
	}
}
