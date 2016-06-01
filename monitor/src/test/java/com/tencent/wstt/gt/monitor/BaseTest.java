package com.tencent.wstt.gt.monitor;

import com.tencent.wstt.gt.monitor.model.AbsOutParam;
import com.tencent.wstt.gt.monitor.model.Group;
import com.tencent.wstt.gt.monitor.simple.LongOutParam;
import com.tencent.wstt.gt.monitor.simple.LongThresholdListener;
import com.tencent.wstt.gt.monitor.threshold.IGTComparator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseTest {

	Queue<Long> queue;

	@Before
	public void setUp()
	{
		// 准备测试用数据源
		queue = new LinkedList<>();
		for (long i = 0; i < 20; i++)
		{
			queue.add(i);
		}
	}

	@Test
	public void testSingleParam()
	{
		// 数据监控对象
		LongOutParam singleParam = new LongOutParam(null, "singleParam");
		singleParam.setRecord(true);
		singleParam.setMonitor(true);

		// 三个用于辅助测试结果检验的数据容器
		final List<Long> higherThanList = mock(ArrayList.class);
		final List<Long> lowerThanList = mock(ArrayList.class);
		final List<Long> equalsList = mock(ArrayList.class);

		/*
		 * 分别设置对关注数据源大于、小于、等于的告警阈值分别为大于10、小于9，等于5
		 */
		singleParam.addThresholdListener(new LongThresholdListener<AbsOutParam<Long>>(10L, 1, 9L, 1, 5L, 1){
			@Override
			public void onHigherThan(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {
				higherThanList.add(data);
			}

			@Override
			public void onLowerThan(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {
				lowerThanList.add(data);
			}

			@Override
			public void onEquals(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {
				equalsList.add(data);
			}});

		// 依次遍历数据源中的数据
		for (Long value : queue)
		{
			singleParam.setValue(value);
		}

		// 校验点，触发大于10的数据告警9次（11~19），小于9的数据告警9次（0~8），等于5的数据告警1次
		verify(higherThanList, times(9)).add(anyLong());
		verify(lowerThanList, times(9)).add(anyLong());
		verify(equalsList, times(1)).add(anyLong());
	}

	@Test
	public void testGroupParam()
	{
		Group<LongOutParam> group = new Group<LongOutParam>("test");
		LongOutParam singleParam = new LongOutParam(null, "singleParam");
		singleParam.setRecord(true);

		group.register(singleParam);
		Assert.assertEquals(group.isEmpty(), false);

		group.clear();
		Assert.assertEquals(group.isEmpty(), true);
	}
}
