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
		queue = new LinkedList<>();
		for (long i = 0; i < 20; i++)
		{
			queue.add(i);
		}
	}

	@Test
	public void testSingleParam()
	{
		LongOutParam singleParam = new LongOutParam(null, "singleParam");
		singleParam.setRecord(true);
		singleParam.setMonitor(true);

		final List<Long> higherThanList = mock(ArrayList.class);
		final List<Long> lowerThanList = mock(ArrayList.class);
		final List<Long> equalsList = mock(ArrayList.class);

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

		for (Long value : queue)
		{
			singleParam.setValue(value);
		}

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
