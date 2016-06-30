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

import com.tencent.wstt.gt.log.logcat.LogLine;
import com.tencent.wstt.gt.util.RuntimeHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SFUtils {
	
	private static Set<Integer> pids = new HashSet<Integer>(2);
	private static Map<Integer, BlockingQueue<Integer>> queues = new HashMap<Integer, BlockingQueue<Integer>>(2);
	private static boolean killed = true;
	private static Process dumpLogcatProcess;

	/**
	 * 启动一个进程的卡顿帧数采集
	 * @param pid 进程号
	 * @return
	 */
	public static BlockingQueue<Integer> startSampleSF(int pid)
	{
		synchronized (SFUtils.class)
		{
			BlockingQueue<Integer> result = queues.get(pid);
			if (result != null)
			{
				return result;
			}

			result = new ArrayBlockingQueue<Integer>(2);
			pids.add(pid);
			queues.put(pid, result);
			if (dumpLogcatProcess == null && killed)
			{
				killed = false;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							filterSF();
						} catch (NumberFormatException e) {
							e.printStackTrace();
							return;
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
					}
				}, "GTSFRunner").start();
			}
			return result;
		}
	}

	/**
	 * 停止一个进程的卡顿帧数采集
	 * @param pid 进程号
	 * @return
	 */
	public static void stopSampleSF(int pid)
	{
		synchronized (SFUtils.class)
		{
			pids.remove(pid);
			queues.remove(pid);
		}

		if (pids.size() == 0) killProcess();
	}

	private static void filterSF() throws NumberFormatException, IOException {
		List<String> args = new ArrayList<String>(
				Arrays.asList("logcat", "-v", "time", "Choreographer:I", "*:S"));

		dumpLogcatProcess = RuntimeHelper.exec(args);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(dumpLogcatProcess.getInputStream()), 8192);

		String line;
		while ((line = reader.readLine()) != null && !killed) {

			// filter "The application may be doing too much work on its main thread."
			if (!line.contains("uch work on its main t")) {
				continue;
			}
			int pid = LogLine.newLogLine(line, false).getProcessId();
			if (! pids.contains(Integer.valueOf(pid))) {
				continue;
			}

			line = line.substring(50, line.length() - 71);
			Integer value = Integer.parseInt(line.trim());

			BlockingQueue<Integer> queue = queues.get(pid);
			queue.offer(value);
		}
		killProcess();
	}

	private static void killProcess() {
		if (!killed) {
			synchronized (SFUtils.class) {
				if (!killed) {
					if (dumpLogcatProcess != null) {
						RuntimeHelper.destroy(dumpLogcatProcess);
						dumpLogcatProcess = null;
					}
					killed = true;
				}
			}
		}
	}

	/**
	 * 检查卡帧设置是否有效
	 * @return
	 */
	public static boolean check() {
		String cmd = "getprop debug.choreographer.skipwarning";
		ProcessBuilder execBuilder = new ProcessBuilder("sh", "-c", cmd);
		execBuilder.redirectErrorStream(true);
		boolean flag = false;
		try {
			Process p = execBuilder.start();
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null) {
				if (line.compareTo("1") == 0) {
					flag = true;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 设置卡帧阈值为1
	 * @return
	 */
	public static void modify() {
		String cmd = "setprop debug.choreographer.skipwarning 1";
		ProcessBuilder execBuilder = new ProcessBuilder("su", "-c", cmd);
		execBuilder.redirectErrorStream(true);
		try {
			execBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 恢复卡帧设置为30
	 */
	public static void recover() {
		String cmd = "setprop debug.choreographer.skipwarning 30";
		ProcessBuilder execBuilder = new ProcessBuilder("su", "-c", cmd);
		execBuilder.redirectErrorStream(true);
		try {
			execBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 软启动以使卡帧设置生效
	 */
	public static void restart()
	{
		String cmd = "setprop ctl.restart surfaceflinger; setprop ctl.restart zygote";
		ProcessBuilder execBuilder = new ProcessBuilder("su", "-c", cmd);
		execBuilder.redirectErrorStream(true);
		try {
			execBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				resultn += getscore(data);

				count += 1;
			} else {
				if (lastdata >= 40) {
					result += resultn * w;
					sscore += resultn;
					count = 0;
					resultn = 0;
				}
				count += 1;
				resultn += getscore(data);

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

	private static double getscore(Long data) {
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
