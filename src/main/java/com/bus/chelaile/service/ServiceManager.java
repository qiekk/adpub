package com.bus.chelaile.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.http.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bus.chelaile.common.AdvCache;
import com.bus.chelaile.common.AnalysisLog;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.model.Platform;
import com.bus.chelaile.model.QueryParam;
import com.bus.chelaile.model.ShowType;
import com.bus.chelaile.model.TypeNumber;
import com.bus.chelaile.model.ads.Station;
import com.bus.chelaile.model.ads.entity.ActiveAdEntity;
import com.bus.chelaile.model.ads.entity.AdEntity;
import com.bus.chelaile.model.ads.entity.BaseAdEntity;
import com.bus.chelaile.model.ads.entity.LineAdEntity;
import com.bus.chelaile.model.ads.entity.LineFeedAdEntity;
import com.bus.chelaile.model.ads.entity.LineRightAdEntity;
import com.bus.chelaile.model.ads.entity.OpenAdEntity;
import com.bus.chelaile.model.ads.entity.StationAdEntity;
import com.bus.chelaile.model.client.ClientDto;
import com.bus.chelaile.model.record.AdPubCacheRecord;
import com.bus.chelaile.model.record.DisplayUserCache;
import com.bus.chelaile.model.rule.version.VersionEntity;
import com.bus.chelaile.mvc.AdvParam;
import com.bus.chelaile.service.impl.ActiveManager;
import com.bus.chelaile.service.impl.DoubleAndSingleManager;
import com.bus.chelaile.service.impl.FeedAdsManager;
import com.bus.chelaile.service.impl.LineDetailsManager;
import com.bus.chelaile.service.impl.LineFeedAdsManager;
import com.bus.chelaile.service.impl.LineRightManager;
import com.bus.chelaile.service.impl.SimpleAdManager;
import com.bus.chelaile.service.impl.OpenManager;
import com.bus.chelaile.service.impl.RideManager;
import com.bus.chelaile.service.impl.SelfManager;
import com.bus.chelaile.service.impl.StationAdsManager;
import com.bus.chelaile.service.impl.WXBannerManager;
import com.bus.chelaile.service.impl.WXFullManager;
import com.bus.chelaile.strategy.UserStrategyJudger;
import com.bus.chelaile.thread.ReloadInvalidAccountIdTimer;
import com.bus.chelaile.util.FlowUtil;
import com.bus.chelaile.util.New;

public class ServiceManager {

    protected static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
    @Autowired
    private LineDetailsManager lineDetailsManager;
    @Autowired
    private StationAdsManager stationAdsManager;
    @Autowired
    private LineRightManager lineRightManager;
    @Autowired
    private SimpleAdManager simpleAdManager;
    @Autowired
    private WXBannerManager wXBannerManager;
    @Autowired
    private WXFullManager wXFullManager;
    @Autowired
    private FeedAdsManager feedAdsManager;
    @Autowired
    private OpenManager openManager;
    @Autowired
    private LineFeedAdsManager lineFeedAdsManager;
    @Autowired
    private DoubleAndSingleManager doubleAndSingleManager;

    @Autowired
    private ActiveManager activeManager;

    @Autowired
    private RideManager rideManager;

    @Autowired
    private SelfManager selfManager;

    @Autowired
    private StartService startService;

    @Autowired
    AdvInvalidService advInvalidService;

    @Autowired
    ReloadInvalidAccountIdTimer reloadInvalidAccountIdTimer;

    //	@Autowired
    //	CommonService commonService;

    @Autowired
    UserStrategyJudger userStrategyJudger;

    // 线路详情最低的广告版本号
    public static final VersionEntity ANDROID_ADS_LINEDETAILS_VERSION = new VersionEntity(3, 13, 0);
    public static final VersionEntity IOS_ADS_LINEDETAILS_VERSION = new VersionEntity(5, 12, 0);

    /**
     * - 单，双栏，首页浮层： - iOS>=5.2.0 - Android>=3.3.0
     */
    public static final VersionEntity ANDROID_ADS_DOUBLE_VERSION = new VersionEntity(3, 3, 0);
    public static final VersionEntity IOS_ADS_DOUBLE_VERSION = new VersionEntity(5, 2, 0);

    public String getClienSucMap(Object obj, String status) {
        ClientDto clientDto = new ClientDto();
        clientDto.setSuccessObject(obj, status);
        try {
            String json = JSON.toJSONString(clientDto, SerializerFeature.BrowserCompatible);
            // JsonBinder.toJson(clientDto, JsonBinder.always);
            return "**YGKJ" + json + "YGKJ##";
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
            return "";
        }
    }

    public String getClienSucMapWithNoHead(Object obj, String status) {
        ClientDto clientDto = new ClientDto();
        clientDto.setSuccessObject(obj, status);
        try {
            String json = JSON.toJSONString(clientDto, SerializerFeature.BrowserCompatible);
            // JsonBinder.toJson(clientDto, JsonBinder.always);
            return json;
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
            return "";
        }
    }

    public String getClientErrMap(String errmsg, String status) {
        ClientDto clientDto = new ClientDto();
        clientDto.setErrorObject(errmsg, status);
        try {
            String json = JSON.toJSONString(clientDto);
            return "**YGKJ" + json + "YGKJ##";
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
            return "";
        }
    }

    public String getClientErrMapWithNoHead(String errmsg, String status) {
        ClientDto clientDto = new ClientDto();
        clientDto.setErrorObject(errmsg, status);
        try {
            String json = JSON.toJSONString(clientDto);
            return json;
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
            return "";
        }
    }

    /**
     * mvc BusAdvAction直接调用,返回给客户端json字符串
     * 
     * @param advParam
     * @param methodName
     * @return
     */
    public String getAdsResponseStr(AdvParam advParam, String methodName) {
        String responseStr = "";

        try {
            Object object = getQueryValue(advParam, methodName);
            if (object == null) {
                responseStr = getClientErrMap("", Constants.STATUS_NO_DATA);
            } else {
                responseStr = getClienSucMap(object, Constants.STATUS_REQUEST_SUCCESS);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            responseStr = getClientErrMap(e.getMessage(), Constants.STATUS_NO_DATA);
        }
        return responseStr;
    }

    /**
     * 根据methodName获取不同类型的广告
     * 
     * @param advParam
     * @param methodName
     * @return BaseAdEntity或者List<BaseAdEntity>
     * @throws Exception
     */
    public Object getQueryValue(AdvParam advParam, String methodName) throws Exception {
        // // 香港不投广告
        VersionEntity tgv = VersionEntity.parseVersionStr(advParam.getV());
        boolean isValidVersion = false;
        if (methodName.equals("getLineDetails") || methodName.equals("getNewLineDetails")) {
            isValidVersion = checkLineDetailsVersion(tgv, Platform.from(advParam.getS()));
        } else {
            isValidVersion = checkDoubleVersion(tgv, Platform.from(advParam.getS()));
        }
        // 版本检测失败
        if (!isValidVersion) {
            logger.info("isValidVersion return false,udid={},s={},v={}", advParam.getUdid(), advParam.getV(), advParam.getS());
            return null;
        }
        Object entity = null;
        JSONObject object = new JSONObject();
        if (methodName.equals("getLineDetails")) { // 线路详情
            long startTime = System.currentTimeMillis();
            object = getLineDetails(advParam);
            logger.info("getLineDetailAds cost time :{}", System.currentTimeMillis() - startTime);
            if (object == null) {
                return null;
            }
            return object;
        } else if (methodName.equals("getLineFeedAds")) { //线路详情页下方的feed流广告。（原feed流顶部广告位）
            entity = getLineFeedAds(advParam);
            object = (JSONObject) entity;
        } else if (methodName.equals("getNewOpen")) { // 新版本开屏、浮层、乘车页浮层
            entity = getNewOpen(advParam);
            object.put("ads", entity);
        } else if (methodName.equals("precacheResource")) { // 预缓存广告资源图片
            entity = precacheResource(advParam);
            object = (JSONObject) entity;
        } else if (methodName.equals("getDoubleAndSingleAds")) { // 单双栏
            entity = getDoubleAndSingleAds(advParam);
            object.put("ads", entity);
        } else if (methodName.equals("getWXBannerAds")) { // 小程序 banner广告
            List<BaseAdEntity> entities = getWXBannerAd(advParam);
            if (entities != null && entities.size() > 0) {
                object.put("ads", entities);
            }
            return object;
        } else if (methodName.equals("getWXFullAds")) { // 小程序浮层广告
            entity = getWXFullAd(advParam);
            if (entity != null) {
                object.put("ads", entity);
            }
            return object;
        } else if (methodName.equals("h5BannerAds")) { // h5 banner广告
            entity = getH5BannerAd(advParam);
            object.put("ads", entity);
            return object;
        } else if (methodName.equals("getFeedAds")) { // feed流广告
            entity = getFeedAds(advParam);
            object.put("ads", entity);
        } else if (methodName.equals("getRide")) {
            entity = getRide(advParam);
            object = (JSONObject) entity; // 乘车页广告，新增音频广告内容
        } else if (methodName.equals("getActive")) { // 活动页|聊天室 广告
            entity = getActive(advParam);
            switch (advParam.getType()) {
                case 0:
                    object.put("activeAds", entity);
                    break;
                case 2:
                    object.put("chatAds", entity);
                    break;
                default:
                    return null;
            }
        } else if (methodName.equals("getAboardText")) { // 上车提醒文案
            entity = getAboardText(advParam);
            object = (JSONObject) entity;
        } else if (methodName.equals("getOldOpen")) { // 旧版本开屏、浮层
            entity = getOldOpen(advParam);
            object.put("ads", entity);
        } else if (methodName.equals("preLoadAds")) { // 旧版本开屏浮层预加载
            entity = preLoadAds(advParam);
            object = (JSONObject) entity;
            // object.put("ads", entity);
        }
        if (entity == null)
            return null;
        return object;
    }

    /*
     * 单双栏
     */
    private List<BaseAdEntity> getDoubleAndSingleAds(AdvParam advParam) throws Exception {
        QueryParam queryParam = new QueryParam();

        if (advParam.getType() == TypeNumber.ONE.getType()) {
            List<BaseAdEntity> routeEntites = doubleAndSingleManager.doServiceList(advParam, ShowType.ROUTE_PLAN_ADV, queryParam);
            List<BaseAdEntity> list = New.arrayList();
            if (routeEntites != null && routeEntites.size() > 0) {
                list.addAll(routeEntites);
            }
            return list;
        }
        List<Station> stList = advParam.getStationList();
        Station lastUnfoldStation = new Station("noUnfold", 0, false);
        Station firstUnfoldStation = new Station("noUnfold", -1, false);
        boolean isFirstUnfoldStation = false;
        for (Station st : stList) {
            if (st.isUnfold()) {
                if (!isFirstUnfoldStation) {
                    firstUnfoldStation = st;
                    isFirstUnfoldStation = true;
                }
                lastUnfoldStation = st;
            }
        }
        queryParam.setStation(lastUnfoldStation); // 将最后一个展开的站点赋值给 queryParam
        BaseAdEntity doubleEntity = doubleAndSingleManager.doService(advParam, ShowType.DOUBLE_COLUMN, false, queryParam, true);
        queryParam.setStation(firstUnfoldStation);
        BaseAdEntity singleEntity = null;
        if (advParam.getlSize() == -1) {
            singleEntity = doubleAndSingleManager.doService(advParam, ShowType.SINGLE_COLUMN, false, queryParam, true);
        }

        if (doubleEntity == null && singleEntity == null) {
            return null;
        }

        List<BaseAdEntity> list = New.arrayList();

        // 首页导流入口广告如果有两层，那么不予返回广告 && 金数据调查问卷 除外
        if (doubleEntity != null) {
            if (advParam.getGridLines() != 2 || doubleEntity.getLink().contains("jinshuju.net")) {
                list.add(doubleEntity);
            } else {
                logger.info("double ads return null, udid={}, cityId={}, gridLines={}", advParam.getUdid(), advParam.getCityId(),
                        advParam.getGridLines());
            }
        }
        if (singleEntity != null) {
            if (advParam.getGridLines() != 2 || singleEntity.getLink().contains("jinshuju.net")) {
                list.add(singleEntity);
            } else {
                logger.info("single ads return null, udid={}, cityId={}, gridLines={}", advParam.getUdid(), advParam.getCityId(),
                        advParam.getGridLines());
            }
        }

        return list;
    }

    /*
     * 新版开屏广告，返回一个list
     */
    public Object getCoopenAds(AdvParam advParam) {
        JSONObject resultMap = new JSONObject();
        // 一些配置项
        settingsConfig(resultMap);
        

        List<BaseAdEntity> entities = openManager.doServiceList(advParam, ShowType.OPEN_SCREEN, new QueryParam());

        if (entities == null || entities.size() == 0) {
            return resultMap;
        } else {
            if (((OpenAdEntity) entities.get(0)).getTimeout() == 0) {
                resultMap.put("ads", entities);
                resultMap.put("timeout", 2000);
            } else {
                resultMap.put("ads", entities);
                resultMap.put("timeout", ((OpenAdEntity) entities.get(0)).getTimeout());
            }
        }
        return resultMap;

    }

    private void settingsConfig(JSONObject resultMap) {
        // 读取配置的‘热启动调用广告的时间间隔’
        settings(resultMap, Constants.INTERVALTIME_KEY, "intervalTime");
        if (!resultMap.containsKey("intervalTime")) {
            logger.error("配置项，intervalTime丢失 ！ ");
            resultMap.put("intervalTime", 30);
        }

        // 读取配置的‘最长开屏时间’
        settings(resultMap, Constants.OPENTIMEOUT_KEY, "openTimeout");
        if (!resultMap.containsKey("openTimeout")) {
            logger.error("配置项，openTimeout 丢失 ！ ");
            resultMap.put("openTimeout", 8001);
        }
        
        // 读取配置的‘最长热启动开屏时间’
        settings(resultMap, Constants.OPENTIMEOUT_HOT_KEY, "hotOpenTimeout");
        if (!resultMap.containsKey("hotOpenTimeout")) {
            logger.error("配置项，hotOpenTimeout 丢失 ！ ");
            resultMap.put("hotOpenTimeout", 4001);
        }
        
    }

    private void settings(JSONObject resultMap, String key, String value) {
        if (StaticAds.SETTINGSMAP.containsKey(key)) {
            String sL = StaticAds.SETTINGSMAP.get(key);
            try {
                resultMap.put(value, Integer.parseInt(sL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 详情页下方feed位广告
     * 
     * @param advParam
     * @return
     */
    private Object getLineFeedAds(AdvParam advParam) {
        JSONObject resultMap = new JSONObject();
        // 是否展开
        if (StaticAds.SETTINGSMAP.containsKey(Constants.SCREENHEIGHT_KEY)) {
            String sL = StaticAds.SETTINGSMAP.get(Constants.SCREENHEIGHT_KEY);
            try {
                if (sL != null && Integer.parseInt(sL) >= advParam.getScreenHeight()) {
                    resultMap.put("unfoldFeed", 0); // 0 小屏手机不展开
                    return resultMap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<BaseAdEntity> entities = lineFeedAdsManager.doServiceList(advParam, ShowType.LINE_FEED_ADV, new QueryParam());

        if (entities == null || entities.size() == 0) {
            return resultMap;
        } else {
            if (((LineFeedAdEntity) entities.get(0)).getAutoInterval() == 0
                    || ((LineFeedAdEntity) entities.get(0)).getMixInterval() == 0) {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", 15000);
                resultMap.put("mixInterval", 5000);
            } else {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", ((LineFeedAdEntity) entities.get(0)).getAutoInterval());
                resultMap.put("mixInterval", ((LineFeedAdEntity) entities.get(0)).getMixInterval());
            }
        }
        resultMap.put("unfoldFeed", 1);


        return resultMap;

    }

    // 新版站点广告（ 原来在getLineDetailAds接口中返回， 现在单拆出来）
    public Object getStationAds(AdvParam advParam) {

        JSONObject resultMap = new JSONObject();

        List<BaseAdEntity> entities = stationAdsManager.doServiceList(advParam, ShowType.STATION_ADV, new QueryParam());

        if (entities == null || entities.size() == 0) {
            resultMap.put("desc", "等车无聊，公交出行有福利哦！");
            resultMap.put("descType", 4);   // 2\3 上车；1\4 福利社
            resultMap.put("autoInterval", 15000);
            resultMap.put("mixInterval", 5000);
            return resultMap;
        } else {
            resultMap.put("ads", entities);
            if (((StationAdEntity) entities.get(0)).getAutoInterval() == 0
                    || ((StationAdEntity) entities.get(0)).getMixInterval() == 0) {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", 15000);
                resultMap.put("mixInterval", 5000);
            } else {
                resultMap.put("autoInterval", ((StationAdEntity) entities.get(0)).getAutoInterval());
                resultMap.put("mixInterval", ((StationAdEntity) entities.get(0)).getMixInterval());
            }
        }
        return resultMap;

    }
    
    // 新版右上角广告（ 原来在getLineDetailAds接口中返回， 现在单拆出来）
    public Object getRightAds(AdvParam advParam) {

        JSONObject resultMap = new JSONObject();

        List<BaseAdEntity> entities = lineRightManager.doServiceList(advParam, ShowType.LINE_RIGHT_ADV, new QueryParam());

        if (entities == null || entities.size() == 0) {
            resultMap.put("autoInterval", 15000);
            resultMap.put("mixInterval", 5000);
            return resultMap;
        } else {
            resultMap.put("ads", entities);
            if (((LineRightAdEntity)entities.get(0)).getAutoInterval() == 0
                    || ((LineRightAdEntity) entities.get(0)).getMixInterval() == 0) {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", 15000);
                resultMap.put("mixInterval", 5000);
            } else {
                resultMap.put("autoInterval", ((LineRightAdEntity) entities.get(0)).getAutoInterval());
                resultMap.put("mixInterval", ((LineRightAdEntity) entities.get(0)).getMixInterval());
            }
        }
        return resultMap;

    }


    // 新版首页广告（原单双栏位置）
    public Object getColumntAds(AdvParam advParam) {

        JSONObject resultMap = new JSONObject();
        List<BaseAdEntity> entities = doubleAndSingleManager.doServiceList(advParam, ShowType.DOUBLE_COLUMN, new QueryParam());

        if (entities == null || entities.size() == 0) {
            return resultMap;
        } else {
            if (((AdEntity) entities.get(0)).getAutoInterval() == 0 || ((AdEntity) entities.get(0)).getMixInterval() == 0) {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", 15000);
                resultMap.put("mixInterval", 5000);
            } else {
                resultMap.put("ads", entities);
                resultMap.put("autoInterval", ((AdEntity) entities.get(0)).getAutoInterval());
                resultMap.put("mixInterval", ((AdEntity) entities.get(0)).getMixInterval());
            }
        }
        return resultMap;

    }

    /**
     * 详情页
     * 
     * @param advParam
     * @return
     * @throws Exception
     */
    private JSONObject getLineDetails(AdvParam advParam) throws Exception {
        JSONObject object = new JSONObject();

        boolean isNeedApid = false;
        QueryParam queryParam = new QueryParam();
        if (advParam.getS().equalsIgnoreCase("ios") && advParam.getVc() >= 10220 && advParam.getScreenHeight() > 0
                && advParam.getScreenHeight() >= 960) {
            isNeedApid = true;
        } else if (advParam.getS().equalsIgnoreCase("android") && advParam.getVc() >= 60 && advParam.getScreenHeight() > 0
                && advParam.getScreenHeight() >= 960) {
            isNeedApid = true;
        }

        BaseAdEntity stnAds = null;
        BaseAdEntity lineAds = null;
        // 2018-05-30后，新版不再调用站点广告
        if (!((advParam.getS().equalsIgnoreCase("ios") && advParam.getVc() >= Constants.PLATFOMR_LOG_IOS_0528)
                || (advParam.getS().equalsIgnoreCase("android") && advParam.getVc() >= Constants.PLATFORM_LOG_ANDROID_0528))) {
            stnAds = stationAdsManager.doService(advParam, ShowType.STATION_ADV, false, queryParam, true);
        }
        if (!((advParam.getS().equalsIgnoreCase("android") && advParam.getVc() >= Constants.PLATFORM_LOG_ANDROID_0605))) {
            lineAds = lineDetailsManager.doService(advParam, ShowType.LINE_DETAIL, isNeedApid, queryParam, true);
        }

        // 0208以前的版本，都只返回一条广告
        // 如果站点和详情页同时存在，需要做一下优先级对比
        if (onlyOneAdCheck(advParam)) {
            if (stnAds != null && lineAds != null) {
                if (((StationAdEntity) stnAds).getPriority() < ((LineAdEntity) lineAds).getPriority()) {
                    object.put("lineAds", lineAds);
                    return object;
                } else {
                    object.put("stationAds", stnAds);
                    return object;
                }
            } else if (lineAds != null) {
                object.put("lineAds", lineAds);
                return object;
            } else if (stnAds != null) {
                object.put("stationAds", stnAds);
                return object;
            }
        }

        if (stnAds != null) {
            object.put("stationAds", stnAds);
        }
        if (null != lineAds) {
            object.put("lineAds", lineAds);
        }
        if (returnRefreshAds(advParam)) {
            BaseAdEntity refreshAds =
                    simpleAdManager.doService(advParam, ShowType.LINEDETAIL_REFRESH_ADV, false, queryParam, true);
            if (refreshAds != null) {
                object.put("reBannerAds", refreshAds);
            } else {
                BaseAdEntity refreshOpenAds =
                        simpleAdManager.doService(advParam, ShowType.LINEDETAIL_REFRESH_OPEN_ADV, false, queryParam, true);
                if (refreshOpenAds != null) {
                    object.put("reBannerAds", refreshOpenAds);
                }
            }
        }
        return object;
    }

    // 0117版本之后，只返回一条广告。 之后，可以多类型广告并存
    private boolean onlyOneAdCheck(AdvParam advParam) {
        if (advParam.getS().equalsIgnoreCase("android") && advParam.getVc() > Constants.PLATFORM_LOG_ANDROID_0118) {
            return false;
        }
        if (advParam.getS().equalsIgnoreCase("ios") && advParam.getVc() > Constants.PLATFORM_LOG_IOS_0117) {
            return false;
        }
        return true;
    }

    /**
     * 根据是否灰度，来判断是否返回这个。
     * 可以放在配置文件中
     */
    private boolean returnRefreshAds(AdvParam advParam) {
        return true;
    }

    /*
     * feed流广告
     */
    public List<BaseAdEntity> getFeedAds(AdvParam advParam) throws Exception {
        QueryParam queryParam = new QueryParam();

        // 从缓存获cshow
        if (StringUtils.isNoneBlank(advParam.getUdid())
                && (advParam.getS().equalsIgnoreCase("android") || (advParam.getS().equalsIgnoreCase("ios")))) {
            String cshowKey = AdvCache.getCshowKey(advParam.getUdid());
            String cshow = (String) CacheUtil.getFromBUSRedis(cshowKey);
            if (cshow != null) {
                if (!cshow.equals(Constants.CSHOW_LINEDETAIL)) {
                    logger.info("cshow not return ads, udid={}, cshow={}", advParam.getUdid(), cshow);
                    return null;
                }
            }

        }
        List<BaseAdEntity> entities = feedAdsManager.doServiceList(advParam, ShowType.FEED_ADV, queryParam);
        // 如果返回为空，那么需要记录 ‘未投放记录’ ,id 记录为 -1
        if (entities == null || entities.size() == 0) {
            List<Integer> ids = New.arrayList();
            ids.add(-1);
            AdPubCacheRecord cacheRecord = feedAdsManager.gainCacheRecord(advParam, ShowType.FEED_ADV);
            cacheRecord.setNoAdHistoryMap(ids, ShowType.FEED_ADV.getType());
            AdvCache.setAdPubRecordToCache(cacheRecord, advParam.getUdid(), ShowType.DOUBLE_COLUMN.getType());
        }

        return entities;
    }

    //	// 1107后的新版支持站点广告
    //	// 版本控制
    //	private boolean returnStnAds(AdvParam advParam) {
    //		return true;
    //	}

    /*
     * 新版本开屏
     */
    private BaseAdEntity getNewOpen(AdvParam advParam) throws Exception {

        QueryParam queryParam = new QueryParam();
        // 乘车页浮层广告
        if (advParam.getType() == 2) {
            return openManager.doService(advParam, ShowType.FULL_SCREEN_RIDE, false, queryParam, true);
        }
        // 共享单车浮层广告
        if (advParam.getType() == 3) {
            return openManager.doService(advParam, ShowType.FULL_SCREEN_MOBIKE, false, queryParam, true);
        }

        // 对于首页浮层和开屏广告。先获取浮层，如果存在浮层广告，那么开屏不调用第三方
        boolean isRecord = true;
        if (advParam.getType() == 0) { // 如果type为0，那么不记录浮层广告的发送记录。
            isRecord = false;
        }
        boolean isNeedApi = true;
        BaseAdEntity baseAdEntity = openManager.doService(advParam, ShowType.FULL_SCREEN, false, queryParam, isRecord);
        if (baseAdEntity != null) {
            isNeedApi = false;
        }

        // 0 开屏, 1 首页浮层 , 2 乘车页浮层
        if (advParam.getType() == 0) {
            return openManager.doService(advParam, ShowType.OPEN_SCREEN, isNeedApi, queryParam, true);
        } else if (advParam.getType() == 1) {
            return baseAdEntity;
        } else {
            throw new IllegalArgumentException("type类型错误:type=" + advParam.getType());
        }

    }

    /*
     * 活动页和聊天室
     */
    private BaseAdEntity getActive(AdvParam advParam) throws Exception {

        QueryParam queryParam = new QueryParam();
        if (advParam.getType() == 0) {
            return activeManager.doService(advParam, ShowType.ACTIVE_DETAIL, false, queryParam, true);
        } else if (advParam.getType() == 2) {
            // 聊天室广告，从数据库获取广告，类型都是8，返回数据中需要区分对待
            return activeManager.doService(advParam, ShowType.RIDE_DETAIL, false, queryParam, true);
        } else {
            throw new IllegalArgumentException("type类型错误:type=" + advParam.getType());
        }
    }

    /*
     * h5 banner 广告
     */
    private BaseAdEntity getH5BannerAd(AdvParam advParam) {
        BaseAdEntity h5BannerAd = simpleAdManager.doService(advParam, ShowType.H5_LINEBANNER_ADV, false, new QueryParam(), true);
        return h5BannerAd;
    }

    /*
     * 小程序 banner 广告
     * isNeedApi = true
     */
    private List<BaseAdEntity> getWXBannerAd(AdvParam advParam) {
        List<BaseAdEntity> wxBannerAds = wXBannerManager.doServiceList(advParam, ShowType.WECHATAPP_BANNER_ADV, new QueryParam());
        return wxBannerAds;
    }

    /*
     * 小程序 浮层 广告
     * isNeedApi = true
     */
    private BaseAdEntity getWXFullAd(AdvParam advParam) {
        BaseAdEntity wxFullAds = wXFullManager.doService(advParam, ShowType.WECHAT_FULL_ADV, false, new QueryParam(), true);
        return wxFullAds;
    }

    /*
     * 乘车页广告单独处理
     */
    private JSONObject getRide(AdvParam advParam) {

        JSONObject resultMap = new JSONObject();
        QueryParam queryParam = new QueryParam();
        BaseAdEntity rideAds = rideManager.doService(advParam, ShowType.RIDE_DETAIL, false, queryParam, true);
        BaseAdEntity audioAds = rideManager.doService(advParam, ShowType.RIDE_AUDIO, false, queryParam, true);

        if (rideAds == null && audioAds == null) {
            return null;
        }
        resultMap.put("rideAds", rideAds);
        resultMap.put("audioAds", audioAds);

        return resultMap;
    }

    

    /*
     * 旧版本预加载
     */
    private JSONObject preLoadAds(AdvParam advParam) throws Exception {

        QueryParam queryParam = new QueryParam();
        queryParam.setOldMany(true);
        BaseAdEntity openEntity = openManager.doService(advParam, ShowType.OPEN_SCREEN, false, queryParam, true);
        BaseAdEntity fullEntity = openManager.doService(advParam, ShowType.FULL_SCREEN, false, queryParam, true);
        if (openEntity == null && fullEntity == null) {
            return null;
        }
        List<BaseAdEntity> list = New.arrayList();
        if (openEntity != null) {
            list.add(openEntity);
        }
        if (fullEntity != null) {
            list.add(fullEntity);
        }
        // 构建返回结构
        JSONObject resultMap = openManager.buildResultMap(list);

        AnalysisLog.info("[PRELOAD_ADS]: tag=9, accountId={}, udid={}, cityId={}, s={}, v={},resultMap={}",
                advParam.getAccountId(), advParam.getUdid(), advParam.getCityId(), advParam.getS(), advParam.getV(), resultMap);

        return resultMap;
    }

    /**
     * 预缓存图片资源 获取今明两天，所有可投广告的图片地址。不需要做规则校验
     * 
     * @param advParam
     * @return
     */
    private Object precacheResource(AdvParam advParam) {
        Set<String> pics = openManager.getAllAdsAdsAudiosPics(advParam, ShowType.OPEN_SCREEN);
        Set<String> picsFull = openManager.getAllAdsAdsAudiosPics(advParam, ShowType.FULL_SCREEN);
        Set<String> audios = openManager.getAllAdsAdsAudiosPics(advParam, ShowType.RIDE_AUDIO);

        JSONObject resultMap = new JSONObject();

        if (pics == null && picsFull == null && audios == null)
            return null;

        if (pics != null) {
            if (picsFull != null) {
                pics.addAll(picsFull);
            }
        } else {
            pics = picsFull;
        }

        resultMap.put("pics", pics);
        resultMap.put("audios", audios);

        //		logger.info("precacheResourceResult: udid={}, pics={}", advParam.getUdid(), pics.toString());

        return resultMap;

    }

    /*
     * 老版开屏，浮层
     */
    private BaseAdEntity getOldOpen(AdvParam advParam) throws Exception {

        QueryParam queryParam = new QueryParam();
        // 0 开屏 1 浮层
        if (advParam.getType() == 0) {
            return openManager.doService(advParam, ShowType.OPEN_SCREEN, false, queryParam, true);
        } else if (advParam.getType() == 1) {
            return openManager.doService(advParam, ShowType.FULL_SCREEN, false, queryParam, true);
        } else {
            throw new IllegalArgumentException("type类型错误:type=" + advParam.getType());
        }

    }

    /*
     * reload
     */
    public String reloadDatas() {
        long start = System.currentTimeMillis();
        if (!SynchronizationControl.isReload()) {
            try {
                SynchronizationControl.setReloadSynLockState(true);
                List<String> advIds = startService.init();
                userStrategyJudger.init();
                JSONObject jsn = new JSONObject();
                jsn.put("advIds", advIds);
                logger.info("reload costs {} ms", (System.currentTimeMillis() - start));
                return getClienSucMap(jsn, Constants.STATUS_REQUEST_SUCCESS);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error("reloadDatas exception");
            } finally {
                SynchronizationControl.setReloadSynLockState(false);
            }
        } else {
            logger.info("reloadDatas repeat failed");
            return getClientErrMap("请稍后再试", "01");
        }
        return null;
    }

    /*
     * uninterest
     */
    public String uninterest(AdvParam param, int showType, int advId, int apiType, String provider_id, String secret) {

        logger.info(
                "[ENTERuninterestads]: showType={}, advId={}, udid={}, accountId={}, cityId={}, lineId={}, apiType={},"
                        + " provider_id={}, secret={}",
                showType, advId, param.getUdid(), param.getAccountId(), param.getCityId(), param.getLineId(), apiType,
                provider_id, secret);

        if (!checkSecret()) {
            logger.error("点击关闭操作非法: advId={}, udid={}, accountId={}, secret={}", advId, param.getUdid(), param.getAccountId(),
                    secret);
            return getClientErrMap("非法操作", Constants.STATUS_INTERNAL_ERROR);
        }

        AdvCache.saveNewUninterestedAds(param.getUdid(), advId, param.getLineId(), showType, provider_id, apiType, param.getS(),
                param.getVc());
        if (showType == 5) {
            // 清除右小角广告投放次数
            selfManager.clearRithPushNum(param.getUdid(), advId);
        }

        return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
    }

    public String interest(AdvParam param, int showType, int advId) {
        AdvCache.removeUninterestAds(param.getUdid(), showType, advId);
        return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
    }

    /*
     * invalidUser
     */
    public String invalidUser(AdvParam param, String startInvalidDate, String endInvalidDate) {
        try {
            logger.info("[ENTERinvalidUsers]: udid={}, accountId={}", param.getUdid(), param.getAccountId());
            advInvalidService.invalidUser(param.getUdid(), param.getAccountId(), startInvalidDate, endInvalidDate);
            logger.info("invalidUser Success: udid={}, accountId={}", param.getUdid(), param.getAccountId());
            return getClientErrMap("", Constants.STATUS_NO_DATA);
        } catch (Exception e) {
            logger.error("invalidUsers Failed: udid={}, accountId={}", param.getUdid(), param.getAccountId());
            return getClientErrMap("", Constants.STATUS_FUNCTION_NOT_ENABLED);
        }
    }

    /*
     * clearInvalidAds
     */
    public String clearInvalidAds(AdvParam param) {
        if (param.getAccountId() == null) {
            return getClientErrMap("accountId为空", Constants.STATUS_PARAM_ERROR);
        }
        try {
            logger.info("[ENTERclearInvalidAds]: udid={}, accountId={}", param.getUdid(), param.getAccountId());
            advInvalidService.clearAccountId(param.getAccountId());
        } catch (Exception e) {
            logger.error(String.format("Uncaught exception: errMsg=%s,udid=%s", e.getMessage(), param.getUdid()), e);
            return getClientErrMap(e.getMessage(), Constants.STATUS_PARAM_ERROR);
        }
        logger.info("清除掉失效的广告用户信息: accountId={}, udid={}", param.getAccountId(), param.getUdid());

        return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
    }

    /*
     * 获取上车提醒文案
     */
    private Object getAboardText(AdvParam advParam) {

        JSONObject resultMap = new JSONObject();
        // 取得所有刻意投放广告
        if (Constants.ISTEST && advParam.getUdid().equals("forPushAboard")) {
            return resultMap;
        }
        QueryParam queryParam = new QueryParam();
        BaseAdEntity audioAds = rideManager.doService(advParam, ShowType.RIDE_AUDIO, false, queryParam, true);
        System.out.println(JSONObject.toJSONString(audioAds));

        if (audioAds != null && audioAds instanceof ActiveAdEntity) {
            ActiveAdEntity ads = (ActiveAdEntity) audioAds;
            String text = ads.getAboardText();
            if (StringUtils.isNoneEmpty(text)) {
                resultMap.put("aboardText", text);
            }
        }

        return resultMap;
    }

    /**
     * load invalidUsers
     */
    public void reloadInvalidUsers() {
        reloadInvalidAccountIdTimer.run();
    }

    /*
     * getDisplayAdv(获取所有可投放的广告)
     */
    public String getDisplayAdv(AdvParam advParam) {
        logger.info("[ENTERgetDisplayAdv] udid={}, accountId={}, cityId={}", advParam.getUdid(), advParam.getAccountId(),
                advParam.getCityId());
        DisplayUserCache displayUserCache = CommonService.getDisplayAdvByUdid(advParam);
        JSONObject js = new JSONObject();
        js.put("lineAds", displayUserCache);

        return getClienSucMap(js, Constants.STATUS_REQUEST_SUCCESS);
    }

    /**
     * 线路详情最低版本检测
     *
     * @param tgtV
     * @param s
     * @return
     */
    private boolean checkLineDetailsVersion(VersionEntity tgtV, Platform platform) {
        VersionEntity baseVersion = null;
        if (platform == Platform.ANDROID) {
            baseVersion = ANDROID_ADS_LINEDETAILS_VERSION;
        } else if (platform == Platform.IOS) {
            baseVersion = IOS_ADS_LINEDETAILS_VERSION;
        } else if (platform == Platform.H5) {
            // H5无版本限制。
            return true;
        } else {
            logger.error("[WRONG_PLATFORM] 无法识别的目标平台: platform={}", platform);
            return false;
        }

        if (tgtV == null) {
            logger.error("目标版本为null");
            return false;
        }

        return tgtV.compareTo(baseVersion) >= 0;
    }

    private boolean checkDoubleVersion(VersionEntity tgtV, Platform platform) {
        VersionEntity baseVersion = null;

        if (platform == Platform.ANDROID) {
            baseVersion = ANDROID_ADS_DOUBLE_VERSION;
        } else if (platform == Platform.IOS) {
            baseVersion = IOS_ADS_DOUBLE_VERSION;
        } else if (platform == Platform.H5) {
            // H5无版本限制。
            return true;
        } else {
            logger.error("[WRONG_PLATFORM] 无法识别的目标平台: platform={}", platform);
            return false;
        }

        if (tgtV == null) {
            logger.error("目标版本为null");
            return false;
        }

        return tgtV.compareTo(baseVersion) >= 0;
    }

    private boolean checkSecret() {
        return true;
    }

    /*
     * 修改从fileName中读取到的所有用户的收藏频道。
     * 增加指定频道channelId
     */
    public String handlFavChannels(String fileName, String channelId) {
        BufferedReader reader = null;
        int size = 0;
        try {
            File checkFile = new File(fileName);
            if (!checkFile.exists()) {
                System.out.println("fileName={} is not exist" + fileName);
                return "file not exit! , file=" + fileName;
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line = reader.readLine();
            while (line != null) {
                line = StringUtils.trimToEmpty(line);
                String buf[] = line.split(",");
                if (buf.length >= 2) {
                    String udid = buf[0];
                    String accountId = buf[1];
                    // 根据用户id，添加channleId到他们的收藏中
                    addFavChannleToUser(udid, accountId, channelId);
                }
                line = reader.readLine();
                size++;
            }
        } catch (IOException ioe) {
            String errMsg = "Read file '" + fileName + "' exception, errMsg=" + ioe.getMessage();
            System.out.println(errMsg);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                // Ignore this exception
            }
        }
        logger.info("handle FavChannel end ,size={}", size);
        return "change FavChannel success! ";
    }

    private void addFavChannleToUser(String udid, String accountId, String channelId) {
        ArrayList<String> favChannelIds = null;
        boolean isFromUDID = true;
        // accountId不为空，那么取之
        if (StringUtils.isNoneEmpty(accountId) && !accountId.equalsIgnoreCase("null")) {
            String key = "article_fav_" + accountId;
            String favStr = (String) CacheUtil.getNew(key);
            // accountId存在收藏，直接返回， 如果不存在，接下来继续看udid
            if (favStr != null) {
                isFromUDID = false;
                favChannelIds = new ArrayList<>(Arrays.asList(favStr.split(",")));
                logger.info("get favArticles from ocs by ACCOUNTID : udid={}, accountId={}, favStr={}", udid, accountId, favStr);
            }
        }

        // 如果accountId取收藏失败，那么从udid入手
        if (favChannelIds == null) {
            String key = "article_fav_" + udid;
            String favStr = (String) CacheUtil.getNew(key);
            // accountId存在收藏，直接返回， 如果不存在，接下来继续看udid
            if (favStr != null) {
                favChannelIds = new ArrayList<>(Arrays.asList(favStr.split(",")));
                logger.info("get favArticles from ocs by UDID : udid={}, accountId={}, favStr={}", udid, accountId, favStr);
            }
        }

        // 处理之！！！（增加channelId到第二位，既‘推荐频道’后面）
        if (favChannelIds != null) {
            if (!favChannelIds.contains(channelId)) {
                favChannelIds.add(1, channelId);
            }
            logger.info("favChannels after change, favStr={}", FlowUtil.changeArrayTOString(favChannelIds));
            String key = "article_fav_" + accountId;
            if (isFromUDID) {
                key = "article_fav_" + udid;
            }
            CacheUtil.setNew(key, -1, FlowUtil.changeArrayTOString(favChannelIds));
            return;
        }

        logger.info("根据udid={},accountId={}, 无法查询到有效的收藏", udid, accountId);
        return;
    }

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, IOException {
        //	    String adStr = "{\"barColor\":\"255,255,255,1\",\"brandIcon\":\"https://pic1.chelaile.net.cn/adv/brandIcon305320170413.png\",\"brandName\":\"车来了\",\"buttonColor\":\"\",\"buttonIcon\":\"https://pic1.chelaile.net.cn/adv/buttonIcon305320170413.png\",\"buttonTitle\":\"\",\"buttonType\":1,\"clickMonitorLink\":\"\",\"distance\":-1,\"head\":\"小车需要你\",\"id\":3053,\"lindex\":0,\"link\":\"https://ad.chelaile.net.cn/?link=https%3A%2F%2Fsojump.com%2Fm%2F13332385.aspx&adtype=00&distance=-1&advId=3053&udid=57f41bf99791d7b3668595bc23eed2b0cae99a2d&storder=1\",\"monitorType\":0,\"openType\":0,\"promoteTitle\":\"调查问卷\",\"provider_id\":\"1\",\"showType\":0,\"sindex\":1,\"subhead\":\"你的意见将直达公路院哦~\",\"targetType\":0,\"type\":1,\"unfoldMonitorLink\":\"\"}";
        //        AdEntity ad = JSON.parseObject(adStr, AdEntity.class);
        AdEntity ad = new AdEntity(ShowType.DOUBLE_COLUMN.getValue());
        ad.setBarColor("255,255,255,1");
        ad.setBrandIcon("https://pic1.chelaile.net.cn/adv/brandIcon305320170413.png");
        ad.setBrandName("车来了");
        ad.setButtonIcon("https://pic1.chelaile.net.cn/adv/buttonIcon305320170413.png");
        ad.setButtonType(1);
        ad.setHead("小车需要你");
        ad.setLink(
                "https://ad.chelaile.net.cn/?link=https%3A%2F%2Fsojump.com%2Fm%2F13332385.aspx&adtype=00&distance=-1&advId=3053&udid=57f41bf99791d7b3668595bc23eed2b0cae99a2d&storder=1");
        ad.setSubhead("你的意见将直达公路院哦~");
        ad.setId(3053);
        System.out.println(JSONObject.toJSONString(ad));
        System.out.println(ad.getBarColor() + "," + ad.getButtonTitle());
        System.exit(0);

    }
}
