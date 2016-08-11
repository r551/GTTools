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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * CPU相关工具类。
 */
public class ProcessCPUUtils {
	/**
	 * 获取CPU使用率，注意本方法执行会停留interval的秒数，所以不要在工作线程中调用
	 * @param pid 指定要采集的进程号
	 * @param interval 采样间隔
	 * @return [进程的CPU使用率, 进程的Jiffies数]
	 */
	public static double[] getUsage(int pid, int interval) {
		double[] result = new double[2];
		String[] resultP = null;
		String[] resultA = null;
		double startPCpu = 0.0;
		double startAllCpu = 0.0;
		double endPCpu = 0.0;
		double endAllCpu = 0.0;
		
		if (pid <= 0) return result;

		resultP = getProcessCpuAction(pid);
		if (null != resultP) {
			startPCpu = Double.parseDouble(resultP[1])
					+ Double.parseDouble(resultP[2]);
		}
		resultA = getCpuAction();
		if (null != resultA) {
			for (int i = 2; i < resultA.length; i++) {

				startAllCpu += Double.parseDouble(resultA[i]);
			}
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

		resultP = getProcessCpuAction(pid);
		if (null != resultP) {
			endPCpu = Double.parseDouble(resultP[1])
					+ Double.parseDouble(resultP[2]);
		}
		resultA = getCpuAction();
		if (null != resultA) {
			for (int i = 2; i < resultA.length; i++) {

				endAllCpu += Double.parseDouble(resultA[i]);
			}
		}

		if ((endAllCpu - startAllCpu) != 0) {
			result[0] = DoubleUtils.div(((endPCpu - startPCpu) * 100.00),
					(endAllCpu - startAllCpu), 2);
			if (result[0] < 0) {
				result[0] = 0;
			}
			else if (result[0] > 100)
			{
				result[0] = 100;
			}
		}
		result[1] = startPCpu;
		return result;
	}

	private static String[] getCpuAction() {
		String cpuPath = "/proc/stat";
		String cpu = "";
		String[] result = new String[7];
		
		File f = new File(cpuPath);
		if (!f.exists() || !f.canRead())
		{
			return null;
		}

		FileReader fr = null;
		BufferedReader localBufferedReader = null;

		try {
			fr = new FileReader(f);
			localBufferedReader = new BufferedReader(fr, 8192);
			cpu = localBufferedReader.readLine();
			if (null != cpu) {
				result = cpu.split(" ");

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileUtil.closeReader(localBufferedReader);
		return result;
	}

	private static String[]  getProcessCpuAction(int pid) {
		String cpuPath = "/proc/" + pid + "/stat";
		String cpu = "";
		String[] result = new String[3];

		File f = new File(cpuPath);
		if (!f.exists() || !f.canRead())
		{
			/*
			 * 进程信息可能无法读取，
			 * 同时发现此类进程的PSS信息也是无法获取的，用PS命令会发现此类进程的PPid是1，
			 * 即/init，而其他进程的PPid是zygote,
			 * 说明此类进程是直接new出来的，不是Android系统维护的
			 */
			return null;
		}

		FileReader fr = null;
		BufferedReader localBufferedReader = null;

		try {
			fr = new FileReader(f);
			localBufferedReader = new BufferedReader(fr, 8192);
			cpu = localBufferedReader.readLine();
			if (null != cpu) {
				String[] cpuSplit = cpu.split(" ");
				result[0] = cpuSplit[1];
				result[1] = cpuSplit[13];
				result[2] = cpuSplit[14];
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		FileUtil.closeReader(localBufferedReader);
		return result;
	}
}
