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

import android.support.test.InstrumentationRegistry;

import com.tencent.wstt.gt.datasource.engine.CPUTimerTask;
import com.tencent.wstt.gt.datasource.engine.DataRefreshListener;
import com.tencent.wstt.gt.datasource.engine.FPSTimerTask;
import com.tencent.wstt.gt.datasource.engine.MEMTimerTask;
import com.tencent.wstt.gt.datasource.engine.NETTimerTask;
import com.tencent.wstt.gt.datasource.engine.PrivateDirtyTimerTask;
import com.tencent.wstt.gt.datasource.engine.PssTimerTask;
import com.tencent.wstt.gt.datasource.engine.SMTimerTask;
import com.tencent.wstt.gt.datasource.engine.UidNETTimerTask;
import com.tencent.wstt.gt.datasource.util.UidNETUtils;

import org.junit.Test;

import java.util.Timer;

public class AllTest {
    @Test
    public void tesAll() throws InterruptedException {
        Timer timer = new Timer();
        Timer timerFPS = new Timer();
        Timer timerSM = new Timer();
        Timer timerCPU = new Timer();

        // FPS
        FPSTimerTask taskFPS = new FPSTimerTask(new DataRefreshListener<Long>() {

            @Override
            public void onRefresh(long time, Long data) {
                System.out.println("FPS:" + data);
            }
        }, true);
        timerFPS.schedule(taskFPS, 0, 1000);

        // SM
        SMTimerTask taskSM = new SMTimerTask(android.os.Process.myPid(), new DataRefreshListener<Long>() {

            @Override
            public void onRefresh(long time, Long data) {
                System.out.println("SM:" + data);
            }
        }, true);
        timerSM.schedule(taskSM, 0, 1000);

        // Other
        CPUTimerTask task2 = new CPUTimerTask(android.os.Process.myPid(), 1000,
                new DataRefreshListener<Double>() {

                    @Override
                    public void onRefresh(long time, Double data) {
                        System.out.println("Process CPU:" + data);
                    }
                },

                new DataRefreshListener<Long>() {

                    @Override
                    public void onRefresh(long time, Long data) {
                        System.out.println("Process Jiffies:" + data);
                    }
                });

        timerCPU.schedule(task2, 0, 1);

        MEMTimerTask task4 = new MEMTimerTask(
                new DataRefreshListener<Long[]>() {

                    @Override
                    public void onRefresh(long time, Long[] data) {
                        System.out.println("MEM:" + data[0] + "/" + data[1] + "/" + data[2] + "/" + data[3]);
                    }
                });

        timer.schedule(task4, 0, 1000);

        PssTimerTask task5 = new PssTimerTask(InstrumentationRegistry.getTargetContext(), android.os.Process.myPid(),
                new DataRefreshListener<Long[]>() {

                    @Override
                    public void onRefresh(long time, Long[] data) {
                        System.out.println("PSS:" + data[0] + "/" + data[1] + "/" + data[2]);
                    }
                });

        timer.schedule(task5, 0, 1000);

        PrivateDirtyTimerTask task6 = new PrivateDirtyTimerTask(InstrumentationRegistry.getTargetContext(), android.os.Process.myPid(),
                new DataRefreshListener<Long[]>() {

                    @Override
                    public void onRefresh(long time, Long[] data) {
                        System.out.println("Private Dirty:" + data[0] + "/" + data[1] + "/" + data[2]);
                    }
                });
        timer.schedule(task6, 0, 1000);

        NETTimerTask task7 = new NETTimerTask(InstrumentationRegistry.getTargetContext(),
                new DataRefreshListener<Double[]>() {
                    @Override
                    public void onRefresh(long time, Double[] data) {
                        System.out.println("NET:" + data[0] + "/" + data[1] + "/" + data[2] + "/" + data[3] + "/" + data[4] + "/" + data[5]);
                    }
                });
        timer.schedule(task7, 0, 1000);

        // 需要先选择适合自己环境的采集方案
        if (!UidNETUtils.test(-1)) {
            UidNETUtils.Case c = new UidNETUtils.CaseTrafficStats();
            if (!c.test(-1)) c = new UidNETUtils.CaseInvalid();
            UidNETUtils.setSampleCase(c);
        }

        UidNETTimerTask task8 = new UidNETTimerTask(1000,
                new DataRefreshListener<Double[]>() {

                    @Override
                    public void onRefresh(long time, Double[] data) {
                        System.out.println("UID NET:" + data[0] + "/" + data[1]);
                    }
                });
        timer.schedule(task8, 0, 1000);
        Thread.sleep(20000);
    }
}
