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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.channels.FileChannel;

public class FileUtil {

	/**
	 * 关闭bufferReader
	 * 
	 * @param br
	 */
	public static void closeReader(Reader br) {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭Writer
	 * 
	 * @param br
	 */
	public static void closeWriter(Writer wr) {
		if (wr != null) {
			try {
				wr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * flush Writer
	 * 
	 * @param br
	 */
	public static void flushWriter(Writer wr) {
		if (wr != null) {
			try {
				wr.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 输入流的关闭
	 * 
	 * @param in
	 */
	public static void closeInputStream(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 输出流的关闭
	 * 
	 * @param out
	 */
	public static void closeOutputStream(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 文件管道的关闭
	 * 
	 * @param in
	 */
	public static void closeFileChannel(FileChannel chl) {
		if (chl != null) {
			try {
				chl.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * RandomAccessFile的关闭
	 * 
	 * @param f RandomAccessFile对象
	 */
	public static void closeRandomAccessFile(RandomAccessFile f)
	{
		if (f != null) {
			try {
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Socket的关闭
	 * 
	 * @param s Socket对象
	 */
	public static void colseSocket(Socket s)
	{
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
