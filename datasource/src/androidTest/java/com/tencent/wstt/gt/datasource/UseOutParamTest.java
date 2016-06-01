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
package com.tencent.wstt.gt.datasource;

import android.support.test.runner.AndroidJUnit4;

import com.tencent.wstt.gt.datasource.engine.CPUTimerTask;
import com.tencent.wstt.gt.datasource.engine.DataRefreshListener;
import com.tencent.wstt.gt.datasource.engine.MEMTimerTask;
import com.tencent.wstt.gt.monitor.model.AbsOutParam;
import com.tencent.wstt.gt.monitor.model.TimeBean;
import com.tencent.wstt.gt.monitor.simple.DoubleOutParam;
import com.tencent.wstt.gt.monitor.simple.DoubleThresholdListener;
import com.tencent.wstt.gt.monitor.simple.LongOutParam;
import com.tencent.wstt.gt.monitor.simple.LongThresholdListener;
import com.tencent.wstt.gt.monitor.threshold.IGTComparator;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Timer;

/**
 * Created by yoyoqin on 2016/5/31.
 */
@RunWith(AndroidJUnit4.class)
public class UseOutParamTest {
    /**
     * 利用出参的阈值能力对超过10%的CPU进行告警
     */
    @Test
    public void testCPUWithOutParamThreshold() throws InterruptedException
    {
        Timer timer = new Timer();
        final DoubleOutParam cpuOutParam = new DoubleOutParam(null, "CPU");
        cpuOutParam.setRecord(true); // 启动记录
        cpuOutParam.setMonitor(true); // 启动告警监控
        // 设置CPU超出10的告警阈值
        cpuOutParam.addThresholdListener(new DoubleThresholdListener<AbsOutParam<Double>>(10.0d, 1, null, 0, null, 0) {
            @Override
            public void onHigherThan(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {
                System.out.println("CPU higher than "+ c.getTarget() + ":" + data);
            }

            @Override
            public void onLowerThan(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {

            }

            @Override
            public void onEquals(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {

            }
        });

        // 进程号填0，关注的即整机的CPU
        CPUTimerTask task = new CPUTimerTask(0, 1000,
                new DataRefreshListener<Double>(){

                    @Override
                    public void onRefresh(long time, Double data) {
                        cpuOutParam.setValue(time, Double.valueOf(data));
                        System.out.println("CPU:" + data);
                    }},null);

        timer.schedule(task, 0, 1);
        Thread.sleep(10000);
        for (TimeBean<Double> timeBean : cpuOutParam.getRecordList())
        {
            System.out.println("CPU:" + timeBean.data + " Time:" + timeBean.time);
        }
    }

    /**
     * 利用出参的阈值能力对超过10%的CPU进行告警
     */
    @Test
    public void testMEMWithOutParamThreshold() throws InterruptedException
    {
        Timer timer = new Timer();

        final LongOutParam memOutParam = new LongOutParam(null, "MEM");

        final LongOutParam memAllOutParam = new LongOutParam(null, "Total");

        final LongOutParam memFreeOutParam = new LongOutParam(null, "Free");
        memFreeOutParam.setRecord(true); // 启动记录
        memFreeOutParam.setMonitor(true); // 启动告警监控
        // 设置Free内存低于800M的告警阈值
        memFreeOutParam.addThresholdListener(new LongThresholdListener<AbsOutParam<Long>>(null, 0, 800L, 1, null, 0) {
            @Override
            public void onHigherThan(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {

            }

            @Override
            public void onLowerThan(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {
                System.out.println("MEM Free lower than "+ c.getTarget() + ", " + c.getCount() + " times:" + data);
            }

            @Override
            public void onEquals(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {

            }
        });

        memOutParam.addChild(memAllOutParam);
        memOutParam.addChild(memFreeOutParam);

        // 进程号填0，关注的即整机的CPU
        MEMTimerTask task = new MEMTimerTask(new DataRefreshListener<Long[]>(){

            @Override
            public void onRefresh(long time, Long[] data) {
                memOutParam.getChild(0).setValue(time, data[0]);
                memOutParam.getChild(1).setValue(time, data[1] + data[2] + data[3]);
                System.out.println("MEM Free/Total:" + (data[1] + data[2] + data[3]) + "/" + data[0]);
            }});

        timer.schedule(task, 0, 1000);
        Thread.sleep(10000);
        for (TimeBean<Long> timeBean : memOutParam.getChild(1).getRecordList())
        {
            System.out.println("MEM Free:" + timeBean.data + " Time:" + timeBean.time);
        }
    }
}
