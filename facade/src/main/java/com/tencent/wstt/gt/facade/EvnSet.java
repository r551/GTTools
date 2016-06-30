package com.tencent.wstt.gt.facade;

import android.content.Context;

/**
 * 环境信息，目前的作用是：
 * 如果是Android环境，需要传入上下文Context
 * 这样在具体的用户接口上不再需要传入Context，可以和非Android环境下的方案保持接口一致
 * Created by yoyoqin on 2016/6/29.
 */
public class EvnSet {
    private EvnSet INSTANCE;
    private Context context;

    public synchronized EvnSet getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new EvnSet();
        }
        return INSTANCE;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public Context getContext()
    {
        return this.context;
    }
}
