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
package com.tencent.wstt.gt.monitor.model;

import com.tencent.wstt.gt.monitor.threshold.IThresholdListener;
import com.tencent.wstt.gt.monitor.threshold.ThresholdEntry;

import java.util.ArrayList;
import java.util.List;

abstract public class AbsOutParam<T extends Comparable> implements Key {
	protected String key;
	protected String alias;
	protected T value;
	protected T freezeValue;

	// 是否记录历史数据
	private boolean record;

	// 是否已进行告警监听
	private boolean monitor;

	// Group代表生命周期
	private Group<Key> group;
	
	public AbsOutParam(Group<Key> group, String key)
	{
		this.group = group;
		if (null != group)
		{
			group.register(this);
		}
		this.key = key;
	}

	private List<AbsOutParam<T>> children;
	private DataRecorder<TimeBean<T>> recorder;
	private ThresholdEntry<AbsOutParam<T>, T> thresholdEntry;

	public Group<Key> getGroup() {
		return group;
	}

	public void setGroup(Group<Key> group)
	{
		this.group = group;
	}

	public String getKey() {
		return this.key;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public T getFreezeValue() {
		return freezeValue;
	}

	public void setFreezeValue(T freezeValue) {
		this.freezeValue = freezeValue;
	}

	public boolean isRecord() {
		return record;
	}

	public void setRecord(boolean record) {
		this.record = record;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		if (null == value) {
			return;
		} else {
			this.value = value;
			this.freezeValue = value;
		}
		if (record && checkValue(value))
		{
			record(System.currentTimeMillis(), value);
		}
		if (monitor)
		{
			threshold(value);
		}
	}

	public void setValue(long time, T value) {
		if (null == value) {
			return;
		} else {
			this.value = value;
			this.freezeValue = value;
		}
		if (record && checkValue(value))
		{
			record(time, value);
		}
		if (monitor)
		{
			threshold(value);
		}
	}

	public synchronized void addChild(AbsOutParam<T> child)
	{
		if (null == children)
		{
			children = new ArrayList<AbsOutParam<T>>(3); // 通常子参数不会超过3个
		}
		children.add(child);
	}

	public synchronized AbsOutParam<T> getChild(int i)
	{
		if (null == children || i >= children.size())
		{
			return null;
		}
		return children.get(i);
	}

	private synchronized void record(long time, T value)
	{
		if (null == recorder)
		{
			recorder = new DataRecorder<TimeBean<T>>();
		}
		TimeBean<T> timeBean = new  TimeBean<T>(time, value);
		recorder.add(timeBean);
	}

	private synchronized void threshold(T value)
	{
		if (monitor && null != thresholdEntry)
		{
			thresholdEntry.add(value);
		}
	}

	public TimeBean<T> getRecord(int seq)
	{
		if (null == recorder)
		{
			return null;
		}
		return recorder.getRecord(seq);
	}

	public ArrayList<TimeBean<T>> getRecordList()
	{
		if (null == recorder)
		{
			return null;
		}
		return recorder.getRecordList();
	}

	public int getRecordSize()
	{
		if (null == recorder)
		{
			return 0;
		}
		return recorder.size();
	}

	public void clearAllRecords() {
		if (children != null && !children.isEmpty())
		{
			for (AbsOutParam<T> p : children)
			{
				p.clearAllRecords();
			}
		}
		if (null == recorder)
		{
			return;
		}
		recorder.clear();
	}

	/**
	 * 增加告警阈值监听
	 * @param listener 发生告警的监听
	 */
	public synchronized void addThresholdListener(
			IThresholdListener<AbsOutParam<T>, T> listener)
	{
		if (null == thresholdEntry)
		{
			thresholdEntry = new ThresholdEntry<AbsOutParam<T>, T>();
		}

		thresholdEntry.addListener(listener);
	}

	/**
	 * 移除告警阈值监听
	 * @param listener 欲移除的告警阈值监听
	 */
	public synchronized void removeThresholdListener(
			IThresholdListener<AbsOutParam<T>, T> listener)
	{
		if (null == thresholdEntry) return;
		thresholdEntry.removeListener(listener);
	}

	/**
	 * 判断输入的值是否有效，只有有效的值才会进行记录和阈值判断
	 * @param value 输入值
	 * @return 是否为有效值
	 */
	abstract public boolean checkValue(T value);
}
