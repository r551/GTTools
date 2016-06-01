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
package com.tencent.wstt.gt.monitor.simple;

import android.support.annotation.Nullable;

import com.tencent.wstt.gt.monitor.threshold.IGTComparator;
import com.tencent.wstt.gt.monitor.threshold.SimpleThresholdListener;

/**
 * Created by yoyoqin on 2016/5/31.
 */
public abstract class DoubleThresholdListener<M> extends SimpleThresholdListener<M, Double> {

    public DoubleThresholdListener(
            @Nullable Double higherThan, int higherD,
            @Nullable Double lowerThan, int lowerD,
            @Nullable Double equals, int equalsD) {
        super(higherThan, higherD, lowerThan, lowerD, equals, equalsD);
    }

    @Override
    public void onHigherThan(M src, Double data, IGTComparator<Double> c) {
        // 按需要覆写
    }

    @Override
    public void onLowerThan(M src, Double data, IGTComparator<Double> c) {
        // 按需要覆写
    }

    @Override
    public void onEquals(M src, Double data, IGTComparator<Double> c) {
        // 按需要覆写
    }

}
