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

public abstract class ThresholdAdaptor<M, T extends Comparable<T>> implements IThresholdListener<M,T> {
	private List<IGTComparator<T>> comparators;
	private M src;

	/**
	 * 扩展用初始方法，后面需要客户代码自己配置比较器
	 */
	public ThresholdAdaptor()
	{
		
	}

	/**
	 * 需要按该构造函数初始化简化的Listener实现类
	 * @param higherThan
	 * @param higherD
	 * @param lowerThan
	 * @param lowerD
	 * @param equals
	 * @param equalsD
	 */
	public ThresholdAdaptor(
			T higherThan, int higherD,
			T lowerThan, int lowerD,
			T equals, int equalsD)
	{
		if (null != higherThan) addGTComparator(new GTComparatorHigherThan<T>(higherThan, higherD<=0 ? 1 : higherD));
		if (null != lowerThan) addGTComparator(new GTComparatorLowerThan<T>(lowerThan, lowerD<=0 ? 1 : lowerD));
		if (null != equals) addGTComparator(new GTComparatorEquals<T>(equals, equalsD<=0 ? 1 : equalsD));
	}

	@Override
	public synchronized void checkValue(T value)
	{
		for (IGTComparator<T> c : comparators)
		{
			if (c.checkTrigger(value))
			{
				waring(src, value, c);
			}
		}
	}

	@Override
	public synchronized void addGTComparator(IGTComparator<T> c)
	{
		if (null == comparators)
		{
			comparators = new ArrayList<IGTComparator<T>>();
		}
		comparators.add(c);
	}

	@Override
	public synchronized void removeGTComparator(IGTComparator<T> c)
	{
		if (null == comparators) return;
		comparators.remove(c);
	}

	@Override
	public abstract void waring(M src, T data, IGTComparator<T> c);
}
