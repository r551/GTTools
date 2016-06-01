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
package com.tencent.wstt.gt.monitor.threshold;

import java.util.ArrayList;
import java.util.List;

public class ThresholdEntry<M, T> implements IThresholdEntry<M, T> {
	private List<IThresholdListener<M, T>> listeners;
	private boolean enable;

	public ThresholdEntry()
	{
	}

	public void add(T value)
	{
		if (null == listeners) return;
		for (IThresholdListener<M, T> listener : listeners)
		{
			listener.checkValue(value);
		}
	}

	/**
	 * 增加告警阈值
	 * @param listener 发生告警的监听
	 * @param comparators 告警比较器，默认有大于、小于、等于这3个比较器，支持自定义扩展
	 */
	@Override
	public synchronized void addListener(
			IThresholdListener<M, T> listener, GTComparator<T>...comparators)
	{
		if (null != comparators)
		{
			for (GTComparator<T> c : comparators)
			{
				listener.addGTComparator(c);
			}
		}

		if (null == listeners)
		{
			listeners = new ArrayList<IThresholdListener<M, T>>();
		}
		listeners.add(listener);
	}

	/**
	 * 增加告警阈值，相比前面方法，加上这个重载方法是为了提高性能
	 * @param listener 发生告警的监听
	 */
	public synchronized void addListener(IThresholdListener<M, T> listener)
	{
		if (null == listeners)
		{
			listeners = new ArrayList<IThresholdListener<M, T>>();
		}
		listeners.add(listener);
	}

	/**
	 * 移除告警阈值监听
	 * @param listener 欲移除的告警阈值监听
	 */
	@Override
	public synchronized void removeListener(IThresholdListener<M,T> listener) {
		if (null != listeners)
		{
			listeners.remove(listener);
		}
	}

	@Override
	public void setEnable(boolean flag) {
		this.enable = flag;
	}

	@Override
	public boolean isEnable() {
		return enable;
	}
}
