package com.bus.chelaile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.model.QueryParam;
import com.bus.chelaile.model.ShowType;
import com.bus.chelaile.model.ads.entity.BaseAdEntity;
import com.bus.chelaile.model.ads.entity.TaskEntity;
import com.bus.chelaile.model.ads.entity.TasksGroup;
import com.bus.chelaile.mvc.AdvParam;
import com.bus.chelaile.service.impl.DoubleAndSingleManager;
import com.bus.chelaile.service.impl.LineFeedAdsManager;
import com.bus.chelaile.service.impl.LineRightManager;
import com.bus.chelaile.service.impl.OpenManager;
import com.bus.chelaile.service.impl.OtherManager;
import com.bus.chelaile.service.impl.StationAdsManager;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;

public class JSService {
    //    @Autowired
    //    private ServiceManager serviceManager;

    @Autowired
    private OpenManager openManager;
    @Autowired
    private StationAdsManager stationAdsManager;
    @Autowired
    private LineFeedAdsManager lineFeedAdsManager;
    @Autowired
    private DoubleAndSingleManager doubleAndSingleManager;
    @Autowired
    private LineRightManager lineRightManager;
    @Resource
    private ServiceManager serviceManager;
    @Autowired
    private OtherManager otherManager;

    protected static final Logger logger = LoggerFactory.getLogger(JSService.class);
    
    private static final ScheduledThreadPoolExecutor fixedThreadPool = new ScheduledThreadPoolExecutor(Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
            "thread.count", "10")));
//    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
//    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
//            "thread.count", "10")));

    public TaskEntity getTask(final AdvParam param, String site) {
        TaskEntity taskEntity = new TaskEntity();
        //        ShowType showType;
        List<BaseAdEntity> entities = null;
        QueryParam queryParam = new QueryParam();
        queryParam.setJS(true);
        switch (site) {
            case "splash":
                entities = openManager.doServiceList(param, ShowType.OPEN_SCREEN, queryParam);
                break;
            case "home":
                entities = doubleAndSingleManager.doServiceList(param, ShowType.DOUBLE_COLUMN, queryParam);
                break;

            case "rightTop":
                entities = lineRightManager.doServiceList(param, ShowType.LINE_RIGHT_ADV, queryParam);
                break;
            case "station":
                entities = stationAdsManager.doServiceList(param, ShowType.STATION_ADV, queryParam);
                break;

            case "bottom":
                entities = lineFeedAdsManager.doServiceList(param, ShowType.LINE_FEED_ADV, queryParam);
                break;

            case "seek":
                entities = otherManager.doServiceList(param, ShowType.SEEK_ADV, queryParam);
                break;

            case "transfer":
                entities = otherManager.doServiceList(param, ShowType.TRANSFER_ADV, queryParam);
                break;

            case "stationDetail":
                entities = otherManager.doServiceList(param, ShowType.CAR_ALL_LINE_ADV, queryParam);
                break;

            case "allCars":
                entities = otherManager.doServiceList(param, ShowType.ALL_CAR_ADV, queryParam);
                break;
            case "interstitialHome":
                entities = otherManager.doServiceList(param, ShowType.INTERSHOME_ADV, queryParam);
                break;
            case "interstitialTransit":
                entities = otherManager.doServiceList(param, ShowType.INTERSTRANSIT_ADV, queryParam);
                break;
            case "interstitialEnergy":
                entities = otherManager.doServiceList(param, ShowType.INTERSENERGY_ADV, queryParam);
                break;
            case "interstitialMine":
                entities = otherManager.doServiceList(param, ShowType.INTERSMINE_ADV, queryParam);
                break;

            default:
                logger.error("未知类型的 site， udid={}, site={}", param);

        }
        //        List<BaseAdEntity> entities = openManager.doServiceList(param, ShowType.OPEN_SCREEN, new QueryParam());
        Map<String,String> map = New.hashMap();
        
        
        List<List<String>> tasks = New.arrayList();
        List<Long> times = New.arrayList();
        String closePic = "";
        String hostSpotSize = "";
        List<Integer> fakeRate = New.arrayList();
        int jsid = 0;
        if (entities != null && entities.size() > 0) {
            for (BaseAdEntity entity : entities) {
                if (entity.getTasksGroup() != null) {
                    // 取任务出来,针对android，去掉imei号是unknown的头条任务
                    tasks.addAll(entity.getTasksGroup().getTasks());
                    // TODO  fist to use stream ， very cool !! 
                    if (StringUtils.isNotBlank(param.getS()) && param.getS().equals("android")) {
                        if (StringUtils.isBlank(param.getImei()) || param.getImei().equals("unknown")) {
                            tasks.removeIf(i -> {
                                return i.contains("sdk_toutiao"); //No contais sdk_toutiao
                            });
                        }

                        // 涉及到非原生的banner的加载问题，跟客户端协议的一个参数
                        if (param.getAdContainerShown() == 0) {
                            tasks.removeIf(i -> {
                                return i.contains("sdk_admobile");
                            });
                        }
                    }

                    //提取第一个times出来
                    if (times.size() == 0)
                        times = entity.getTasksGroup().getTimeouts();
                    if (jsid == 0)
                        jsid = entity.getId();

                    if (entity.getTasksGroup() != null && entity.getTasksGroup().getMap() != null) {
                        for (Map.Entry<String, String> entry : entity.getTasksGroup().getMap().entrySet()) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                    // 将非空的closePic提取出来
                    if (StringUtils.isNoneBlank(entity.getTasksGroup().getClosePic()))
                        closePic = entity.getTasksGroup().getClosePic();
                    if (StringUtils.isNoneBlank(entity.getTasksGroup().getHostSpotSize()))
                        hostSpotSize = entity.getTasksGroup().getHostSpotSize();
                    if (entity.getTasksGroup().getFakeRate() != null && ! entity.getTasksGroup().getFakeRate().isEmpty())
                        fakeRate = entity.getTasksGroup().getFakeRate();
                }
            }
            // 执行setTraceInfo的操作
            executeSetTraceInfo(param, entities);
            
            
        }
        taskEntity.setTaskGroups(new TasksGroup(tasks, times,map, closePic, hostSpotSize, fakeRate));
        taskEntity.setTraceid(param.getTraceid());
        taskEntity.setJsid(jsid);
        JSONObject resultMap = new JSONObject();
        resultMap.put("ads", entities);
        taskEntity.setAdDataString(JSONObject.toJSONString(serviceManager.getClienSucMap(resultMap, Constants.STATUS_REQUEST_SUCCESS)));
        return taskEntity;
    }

    private void executeSetTraceInfo(AdvParam param, List<BaseAdEntity> entities) {
        if (StringUtils.isBlank(param.getTraceid())) {
            //            logger.info("traceid为空 ┭┮﹏┭┮");
            param.setTraceid(param.getUdid() + "_" + System.currentTimeMillis());
        }
//        if (entities.size() == 1 && entities.get(0).getProvider_id().equals("1")) { // js广告，只返回一条。那么是‘自采买广告’
//            logger.info("setTraceInfo, pid={}, udid={}, adId={}", entities.get(0).getShowType(), param.getUdid(), entities.get(0).getId());
//            // 存储atraceInfo到redis中
//            final String traceInfo = JSONObject.toJSONString(param);
//            fixedThreadPool.execute(new Runnable() {
//
//                @Override
//                public void run() {
//                    CacheUtil.setToAtrace(param.getTraceid(), traceInfo, Constants.ONE_HOUR_TIME * 8);
//
//                }
//            });
//        }
    }

    public static void main(String[] args) {
        List<String> t1 = new ArrayList<String>();
        t1.add("sdk_toutiao");

        List<String> t2 = new ArrayList<String>();
        t2.add("sdk_baidu");

        List<List<String>> task = new ArrayList<>();
        task.add(t1);
        task.add(t2);

        System.out.println(task);

        task.removeIf(i -> {
            return i.contains("sdk_toutiao"); //No contais sdk_toutiao
        });
        System.out.println(task);
    }
}
