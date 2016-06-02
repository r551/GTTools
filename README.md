# GTTools
#### Headers
GTTools工具包是从[Android GT]源码中剥离并重构出来的可复用模块，在抽象过程中尽可能的考虑可扩展性。GTTools工具包主要提供给基于AndroidJUnit的测试脚本用于性能指标的采集和数据监控。
#### 当前版本
v1.0
#### 模块介绍
##### monitor
依赖JDK，兼容Android环境和普通Java环境，从Android GT源码中剥离并简化出来的数据监控模块。有较好的扩展性，可以独立使用，在GTTool中辅助数据采集模块datasource进行已采集数据的监控。
##### datasource
数据采集模块。因为流畅度的采集，依赖于log模块。可选monitor模块做为数据监控方，也可以自定义已采集数据的监控，如简单的日志输出。
##### util
Android相关的轻量工具包，依赖Android SDK，无独立使用价值。
##### log
LogCat日志解析模块，从CatLog精简而来。依赖util模块，一般不会独立使用。
### DEMO
##### monitor模块的使用
完整测试代码在monitor/src/test目录中
```java
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
  Group<Key> group = new Group<Key>("test");
  LongOutParam singleParam = new LongOutParam(group, "singleParam");
  singleParam.setRecord(true);

  Assert.assertEquals(group.isEmpty(), false);

  group.clear();
  Assert.assertEquals(group.isEmpty(), true);
}
```
##### datasource模块的使用
完整测试代码在datasource/src/androidTest/UseOutParamTest类中
```java
/**
 * 利用出参的阈值能力对超过10%的CPU进行告警
 */
@Test
public void testCPUWithOutParamThreshold() throws InterruptedException
{
    // 创建用于监控CPU指标的对象
    final DoubleOutParam cpuOutParam = new DoubleOutParam(null, "CPU");
    cpuOutParam.setRecord(true); // 启动记录
    cpuOutParam.setMonitor(true); // 启动告警监控
    /*
     * 设置CPU超出10的告警阈值
     */
    cpuOutParam.addThresholdListener(new DoubleThresholdListener<AbsOutParam<Double>>(10.0d, 1, null, 0, null, 0) {
        @Override
        public void onHigherThan(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {
            // 当CPU超过10时，触发该回调，输出下面的信息到控制台
            System.out.println("CPU higher than "+ c.getTarget() + ":" + data);
        }

        @Override
        public void onLowerThan(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {

        }

        @Override
        public void onEquals(AbsOutParam<Double> src, Double data, IGTComparator<Double> c) {

        }
    });

    /*
     * 初始化CPU数据采集任务，监听中使用前面创建的cpuOutParam对象对CPU数据进行监控
     * 进程号填0，关注的即整机的CPU
     */
    CPUTimerTask task = new CPUTimerTask(0, 1000,
            new DataRefreshListener<Double>(){

                @Override
                public void onRefresh(long time, Double data) {
                    cpuOutParam.setValue(time, Double.valueOf(data));
                    System.out.println("CPU:" + data);
                }},null);

    // 启动定时任务，注意因为任务本身有1000ms的时间间隔，所以定时任务的间隔填最小的1ms
    // CPU指标的采集之所以如此设计，是为了和其他指标采集方式保持一致
    Timer timer = new Timer();
    timer.schedule(task, 0, 1);
    Thread.sleep(10000);

    // 打印此时已记录的CPU历史数据
    for (TimeBean<Double> timeBean : cpuOutParam.getRecordList())
    {
        System.out.println("CPU:" + timeBean.data + " Time:" + timeBean.time);
    }
}

/**
 * 利用出参的阈值能力对手机内存低于800MB时进行告警
 */
@Test
public void testMEMWithOutParamThreshold() throws InterruptedException
{
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
            // 当Free内存低于800MB时会触发此告警向控制台输出打印信息
            System.out.println("MEM Free lower than "+ c.getTarget() + ", " + c.getCount() + " times:" + data);
        }

        @Override
        public void onEquals(AbsOutParam<Long> src, Long data, IGTComparator<Long> c) {

        }
    });

    memOutParam.addChild(memAllOutParam);
    memOutParam.addChild(memFreeOutParam);

    // 对手机整机的内存数据进行监控
    MEMTimerTask task = new MEMTimerTask(new DataRefreshListener<Long[]>(){

        @Override
        public void onRefresh(long time, Long[] data) {
            memOutParam.getChild(0).setValue(time, data[0]);
            memOutParam.getChild(1).setValue(time, data[1] + data[2] + data[3]);
            System.out.println("MEM Free/Total:" + (data[1] + data[2] + data[3]) + "/" + data[0]);
        }});

    // 启动定时任务，因为内存数据是即时值，所以定时1000ms采集一次
    Timer timer = new Timer();
    timer.schedule(task, 0, 1000);
    Thread.sleep(10000);

    // 打印此时已记录的剩余内存历史数据
    for (TimeBean<Long> timeBean : memOutParam.getChild(1).getRecordList())
    {
        System.out.println("MEM Free:" + timeBean.data + " Time:" + timeBean.time);
    }
}
```
[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/stAore-comments-in-markdown-syntax)

[Android GT]: <https://github.com/TencentOpen/GT>
