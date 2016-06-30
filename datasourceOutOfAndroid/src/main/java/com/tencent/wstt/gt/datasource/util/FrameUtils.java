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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FrameUtils {

	private static Process process;
	private static DataOutputStream os;
	private static BufferedReader ir;

	/**
	 * 获取总的帧数，这是计算帧率的数据源
	 * @return 从service call SurfaceFlinger 1013命令中得到的累积帧数
	 * @throws IOException
	 */
	public static synchronized int getFrameNum() throws IOException {
		String frameNumString = "";
		String getFps40 = "service call SurfaceFlinger 1013";
		
		if (process == null)
		{
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			ir = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
		}

		os.writeBytes(getFps40 + "\n");
		os.flush();

		String str = "";
		int index1 = 0;
		int index2 = 0;
		while ((str = ir.readLine()) != null) {
			if (str.indexOf("(") != -1) {
				index1 = str.indexOf("(");
				index2 = str.indexOf("  ");

				frameNumString = str.substring(index1 + 1, index2);
				break;
			}
		}

		int frameNum;
		if (!frameNumString.equals("")) {
			frameNum = Integer.parseInt(frameNumString, 16);
		} else {
			frameNum = 0;
		}
		return frameNum;
	}
	
	public static synchronized void destory()
	{
		try {
			os.writeBytes("exit\n");
			os.flush();
			os.close();
			ir.close();
		} catch (IOException e) {
		}
		process.destroy();
		process = null;
	}
}
