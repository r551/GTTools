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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.telephony.TelephonyManager;

public class NETUtils {
	private static final int TYPE_WIFI = 0;
	private static final int TYPE_FAST = 1;
	private static final int TYPE_GPRS = 2;
	private static final double B2K = 1024.00d;
	
	/**
	 * 获取网络连接类型
	 * 
	 * @return -1表示没有网络
	 */
	public static final int getNetWorkType(Context c) {
		ConnectivityManager conn = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conn == null) {
			return -1;
		}
		NetworkInfo info = conn.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return -1;
		}
		int type = info.getType();
		if (type == ConnectivityManager.TYPE_WIFI) {
			return TYPE_WIFI;
		} else {
			TelephonyManager tm = (TelephonyManager) c
					.getSystemService(Context.TELEPHONY_SERVICE);
			switch (tm.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return TYPE_GPRS;
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return TYPE_GPRS;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return TYPE_GPRS;
			default:
				return TYPE_FAST;
			}
		}
	}

	/**
	 * 获取整体的网络接收流量，包括wifi和Mobile
	 * 
	 * @return 总字节数
	 */
	public static long getNetRxTotalBytes() {
		long total = TrafficStats.getTotalRxBytes();
		return total;
	}

	/**
	 * 获取整体的网络输出流量，包括wifi和Mobile
	 * 
	 * @return 总字节数
	 */
	public static long getNetTxTotalBytes() {
		long total = TrafficStats.getTotalTxBytes();
		return total;
	}

	public static long getNetTxMobileBytes() {
		long total = TrafficStats.getMobileTxBytes();
		return total;
	}

	public static long getNetRxMobileBytes() {
		long total = TrafficStats.getMobileRxBytes();
		return total;
	}

	public static long getNetTxWifiBytes() {
		long total = getNetTxTotalBytes() - getNetTxMobileBytes();
		return total;
	}

	public static long getNetRxWifiBytes() {
		long total = getNetRxTotalBytes() - getNetRxMobileBytes();
		return total;
	}

	/**
	 * 获取整体的网络接收流量，包括wifi和Mobile
	 * 
	 * @return 总数据包数
	 */
	public static long getNetRxTotalPackets() {
		long total = TrafficStats.getTotalRxPackets();
		return total;
	}

	/**
	 * 获取整体的网络输出流量，包括wifi和Mobile
	 * 
	 * @return 总数据包数
	 */
	public static long getNetTxTotalPackets() {
		long total = TrafficStats.getTotalRxPackets();
		return total;
	}

	private static long t_base_wifi = 0;
	private static long t_base_3G = 0;
	private static long t_base_2G = 0;
	private static long r_base_wifi = 0;
	private static long r_base_3G = 0;
	private static long r_base_2G = 0;

	private static double t_add_wifi = 0;
	private static double t_add_3G = 0;
	private static double t_add_2G = 0;
	private static double r_add_wifi = 0;
	private static double r_add_3G = 0;
	private static double r_add_2G = 0;

	public static double getT_add_wifi() {
		return t_add_wifi;
	}

	public static double getT_add_3G() {
		return t_add_3G;
	}

	public static double getT_add_2G() {
		return t_add_2G;
	}

	public static double getR_add_wifi() {
		return r_add_wifi;
	}

	public static double getR_add_3G() {
		return r_add_3G;
	}

	public static double getR_add_2G() {
		return r_add_2G;
	}

	/**
	 * 归零
	 */
	public static void initNetValue() {
		t_base_wifi = getNetTxWifiBytes();
		t_base_3G = t_base_2G = getNetTxMobileBytes();
		r_base_wifi = getNetRxWifiBytes();
		r_base_3G = r_base_2G = getNetRxMobileBytes();

		t_add_wifi = 0;
		t_add_3G = 0;
		t_add_2G = 0;
		r_add_wifi = 0;
		r_add_3G = 0;
		r_add_2G = 0;
	}

	private static long t_cur_wifi = 0;
	private static long t_cur_3G = 0;
	private static long t_cur_2G = 0;
	private static long r_cur_wifi = 0;
	private static long r_cur_3G = 0;
	private static long r_cur_2G = 0;

	/**
	 * 获取当前流量值
	 * @param c
	 * @return {t_add_wifi, r_add_wifi, t_add_3G, r_add_3G, t_add_2G, r_add_2G}
	 */
	public static double[] getNetTxRxKB(Context c) {
		double[] result = new double[6];
		int cur_net_type = getNetWorkType(c);

		switch (cur_net_type) {
		case TYPE_WIFI:
			t_cur_wifi = getNetTxWifiBytes();
			r_cur_wifi = getNetRxWifiBytes();
			t_add_wifi = (t_cur_wifi - t_base_wifi) / B2K;
			r_add_wifi = (r_cur_wifi - r_base_wifi) / B2K;
			break;
		case TYPE_FAST:
			t_cur_3G = getNetTxMobileBytes();
			r_cur_3G = getNetRxMobileBytes();
			t_add_3G = (t_cur_3G - t_base_3G) / B2K;
			r_add_3G = (r_cur_3G - r_base_3G) / B2K;
			break;
		case TYPE_GPRS:
			t_cur_2G = getNetTxMobileBytes();
			r_cur_2G = getNetRxMobileBytes();
			t_add_2G = (t_cur_2G - t_base_2G) / B2K;
			r_add_2G = (r_cur_2G - r_base_2G) / B2K;
			break;
		}

		result[0] = t_add_wifi;
		result[1] = r_add_wifi;
		result[2] = t_add_3G;
		result[3] = r_add_3G;
		result[4] = t_add_2G;
		result[5] = r_add_2G;
		return result;
	}
}
