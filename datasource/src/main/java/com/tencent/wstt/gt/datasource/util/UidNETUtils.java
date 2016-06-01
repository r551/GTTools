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

import android.net.TrafficStats;

import com.tencent.wstt.gt.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class UidNETUtils {
	
	/**
	 * 流量采集方案，可自定义扩展
	 * @author yoyoqin
	 */
	public static interface Case {
		long getTxBytes(int uid) throws Exception;
		long getRxBytes(int uid) throws Exception;
		boolean test(int uid);
	}

	public static class CaseInvalid implements Case {

		@Override
		public long getTxBytes(int uid) throws Exception {
			return 0;
		}

		@Override
		public long getRxBytes(int uid) throws Exception {
			return 0;
		}

		@Override
		public boolean test(int uid) {
			return false;
		}
	}

	public static class CaseUidStat implements Case {

		@Override
		public long getTxBytes(int uid) throws Exception {
			String netPath = "/proc/uid_stat/" + uid + "/tcp_snd";
			File f = new File(netPath);
			if (!f.exists()) {
				throw new Exception(netPath + "not found.");
			}
			else
			{
				FileReader fr = new FileReader(netPath);
				BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
				String ret = localBufferedReader.readLine();
				FileUtil.closeReader(localBufferedReader);
				return Long.parseLong(ret);
			}
		}

		@Override
		public long getRxBytes(int uid) throws Exception {
			String netPath = "/proc/uid_stat/" + uid + "/tcp_rcv";

			File f = new File(netPath);
			if (!f.exists()) {
				throw new Exception(netPath + "not found.");
			}
			else
			{
				FileReader fr = new FileReader(netPath);
				BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
				String ret = localBufferedReader.readLine();
				FileUtil.closeReader(localBufferedReader);
				return Long.parseLong(ret);
			}
		}

		@Override
		public boolean test(int uid) {
			int testUid = uid <= 0 ? 1000 : uid;
			try {
				getRxBytes(testUid);
				getTxBytes(testUid);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}

	public static class CaseTrafficStats implements Case {

		@Override
		public long getTxBytes(int uid) throws Exception {
			return TrafficStats.getUidTxBytes(uid);
		}

		@Override
		public long getRxBytes(int uid) throws Exception {
			return TrafficStats.getUidRxBytes(uid);
		}

		@Override
		public boolean test(int uid) {
			int testUid = uid <= 0 ? 1000 : uid;
			try {
				long n = getRxBytes(testUid) + getTxBytes(testUid);
				if (n <= 0)
				{
					return false;
				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}
//==============================================================================
	private static Case sampleCase = new CaseUidStat();

	public static Case getSampleCase() {
		return sampleCase;
	}

	public static void setSampleCase(Case newCase) {
		sampleCase = newCase;
	}

	private static final double B2K = 1024.00d;

	/**
	 * 获取上下行流量，单位为KB
	 * @param uid 目标uid号，如果输入0或负数，则默认用uid=1000(system server)来测试
	 * @return ｛tx,rx｝
	 * @throws Exception
	 */
	public static double[] getTxRxKB(int uid) throws Exception {
		return new double[]{sampleCase.getTxBytes(uid) / B2K, sampleCase.getRxBytes(uid) / B2K};
	}

	/**
	 * 获取上下行流量，单位为Byte
	 * @param uid 目标uid号，如果输入0或负数，则默认用uid=1000(system server)来测试
	 * @return ｛tx,rx｝
	 * @throws Exception
	 */
	public static long[] getTxRxBytes(int uid) throws Exception {
		return new long[]{sampleCase.getTxBytes(uid), sampleCase.getRxBytes(uid)};
	}

	/**
	 * 测试本方案是否可用
	 * @param uid 测试uid号，如果输入0或负数，则默认用uid=1000(system server)来测试
	 * @return
	 */
	public static boolean test(int uid)
	{
		return sampleCase.test(uid);
	}
}
