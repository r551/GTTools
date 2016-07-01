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
import com.tencent.wstt.gt.datasource.engine.SMTimerTask;
import com.tencent.wstt.gt.datasource.util.SMUtils;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class SMTest {
	@Test
	public void testSMBase() throws InterruptedException
	{
		Timer timer = new Timer();

		SMTimerTask taskSM = new SMTimerTask(new DataRefreshListener<Long>() {

			@Override
			public void onRefresh(long time, Long data) {
				System.out.println("SM:" + data);
			}
		});
		timer.schedule(taskSM, 1000, 1000); // 初始执行的时候也要延迟1000，因为立即采集的数据是0
		Thread.sleep(10000);
	}

	@Test
	public void testSMStop() throws InterruptedException
	{
		Timer timer = new Timer();

		SMTimerTask taskSM = new SMTimerTask(new DataRefreshListener<Long>() {

			@Override
			public void onRefresh(long time, Long data) {
				System.out.println("SM:" + data);
			}
		});
		timer.schedule(taskSM, 1000, 1000);
		Thread.sleep(5000);
		taskSM.stop();
		Thread.sleep(5000);
	}

	@Test
	public void testGetSMDetail() throws InterruptedException
	{
		// 准备一组假数据
		List<Long> lst = new ArrayList<Long>();
		for (int i = 0; i < 100; i++)
		{
			lst.add(Long.valueOf(60));
		}

		lst.add(Long.valueOf(10));

		for (int i = 0; i < 100; i++)
		{
			lst.add(Long.valueOf(60));
		}

		int[] result = SMUtils.getSmDetail(lst);
		Assert.assertTrue(result[5] < 95);
		Assert.assertEquals(result[1], 5);
	}
}
