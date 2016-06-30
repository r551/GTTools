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

import java.lang.reflect.Method;

/**
 * 内存信息工具类。
 */
public class MEMUtils {
	/**
	 * 获取内存信息：total、free、buffers、cached，单位MB
	 * 
	 * @return 内存信息｛total,free,buffers,cached｝
	 */
	public static long[] getPhoneMem() {
		long memInfo[] = new long[4];
		try {
			Class<?> procClazz = Class.forName("android.os.Process");
			Class<?> paramTypes[] = new Class[] { String.class, String[].class,
					long[].class };
			Method readProclines = procClazz.getMethod("readProcLines",
					paramTypes);
			Object args[] = new Object[3];
			final String[] memInfoFields = new String[] { "MemTotal:",
					"MemFree:", "Buffers:", "Cached:" };
			long[] memInfoSizes = new long[memInfoFields.length];
			memInfoSizes[0] = 30;
			memInfoSizes[1] = -30;
			args[0] = new String("/proc/meminfo");
			args[1] = memInfoFields;
			args[2] = memInfoSizes;
			if (null != readProclines) {
				readProclines.invoke(null, args);
				for (int i = 0; i < memInfoSizes.length; i++) {
					memInfo[i] = memInfoSizes[i] / 1024;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return memInfo;
	}

	/**
	 * TODO 通过解析dumpsys meminfo方式获取进程内存的PSS和Private Dirty数据
	 * =============================================================================================
	 ** MEMINFO in pid 2057 [android.process.media] **
	 Pss  Private  Private  Swapped     Heap     Heap     Heap
	 Total    Dirty    Clean    Dirty     Size    Alloc     Free
	 ------   ------   ------   ------   ------   ------   ------
	 Native Heap     2762     2692        0        0     5888     4326     1561
	 Dalvik Heap      976      944        0        0     1150      923      227
	 Dalvik Other      317      316        0        0
	 Stack       96       96        0        0
	 Other dev        4        0        4        0
	 .so mmap      413       56       16        0
	 .apk mmap      126        0       84        0
	 .dex mmap      856        8      848        0
	 .oat mmap     1267        0      400        0
	 .art mmap      980      616        0        0
	 Other mmap      124        8       64        0
	 Unknown       73       72        0        0
	 TOTAL     7994     4808     1416        0     7038     5249     1788
	 *==============================================================================================
	 *
	 * @param pname 进程名
	 * @return {nativePrivateDirty,dalvikPrivateDirty,TotalPrivateDirty}
	 */
	public static long[] getProcessMem(String pname) {
		return null;
	}
}
