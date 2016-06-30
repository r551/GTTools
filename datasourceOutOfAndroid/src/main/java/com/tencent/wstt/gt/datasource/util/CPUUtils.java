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

import com.tencent.wstt.gt.util.DoubleUtils;
import com.tencent.wstt.gt.util.FileUtil;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * CPU相关工具类。
 */
public class CPUUtils {
	/**
	 * 获取CPU使用率，注意本方法执行会停留interval的毫秒数，所以不要在工作线程中调用
	 * @param interval 采样间隔
	 * @return 手机整机的CPU使用率
	 */
	public static double getUsage(int interval) {
		double usage = 0.0;
		double start_cpu = 0.0;
		double start_idle = 0.0;
		double end_cpu = 0.0;
		double end_idle = 0.0;
		RandomAccessFile reader = null;
		try {
			reader = new RandomAccessFile("/proc/stat",
					"r");
			String load = reader.readLine();
			String[] toks = load.split(" ");
			start_idle = Double.parseDouble(toks[5]);
			start_cpu = Double.parseDouble(toks[2])
					+ Double.parseDouble(toks[3])
					+ Double.parseDouble(toks[4])
					+ Double.parseDouble(toks[6])
					+ Double.parseDouble(toks[8])
					+ Double.parseDouble(toks[7]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			FileUtil.closeRandomAccessFile(reader);
		}

		// 采样间隔不能太短，否则会造成性能空耗
		if (interval < 10)
		{
			interval = 1000;
		}
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			reader = new RandomAccessFile("/proc/stat",
					"r");
			String load = reader.readLine();
			String[] toks = load.split(" ");
			end_idle = Double.parseDouble(toks[5]);
			end_cpu = Double.parseDouble(toks[2])
					+ Double.parseDouble(toks[3])
					+ Double.parseDouble(toks[4])
					+ Double.parseDouble(toks[6])
					+ Double.parseDouble(toks[8])
					+ Double.parseDouble(toks[7]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			FileUtil.closeRandomAccessFile(reader);
		}

		if (0 != ((start_idle + start_cpu) - (end_idle + end_cpu))) {
			usage = DoubleUtils.div((100.00 * ((end_cpu - start_cpu))),
					((end_cpu + end_idle) - (start_cpu + start_idle)), 2);
			// 修正4.x之前的系统bug数据
			if (usage < 0) {
				usage = 0;
			}
			else if (usage > 100)
			{
				usage = 100;
			}
		}
		return usage;
	}
}
