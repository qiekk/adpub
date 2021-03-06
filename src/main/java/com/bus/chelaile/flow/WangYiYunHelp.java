package com.bus.chelaile.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import scala.collection.mutable.StringBuilder;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.AdvCache;
import com.bus.chelaile.common.AnalysisLog;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.flow.model.ChannelType;
import com.bus.chelaile.flow.model.FlowChannel;
import com.bus.chelaile.flow.model.FlowContent;
import com.bus.chelaile.flow.wangyiyun.WangYIParamForSignature;
import com.bus.chelaile.flow.wangyiyun.WangYiYunChannelDto;
import com.bus.chelaile.flow.wangyiyun.WangYiYunDetailModel;
import com.bus.chelaile.flow.wangyiyun.WangYiYunErrorCode;
import com.bus.chelaile.flow.wangyiyun.WangYiYunListDataDto;
import com.bus.chelaile.flow.wangyiyun.WangYiYunListModel;
import com.bus.chelaile.flow.wangyiyun.WangYiYunResultBaseDto;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.mvc.AdvParam;
import com.bus.chelaile.util.AdvUtil;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;
import com.google.gson.reflect.TypeToken;

public class WangYiYunHelp extends AbstractWangYiYunHelp {
	@Autowired
	private ActivityService activityService;
	private static final String DEFAULT_CHANNEL = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"wangyi.default.channel", "1852");	 // 推荐频道
	
	// 1852,推荐； 2663，车来了；3385，车来了2；
	
	@Override
	public List<FlowContent> getInfoByApi(AdvParam advParam, long ftime, String recoid, int id, boolean isShowAd)
			throws Exception {
		
		FlowChannel wuliChannel = activityService.getChannels(id, ChannelType.WANGYI);
		List<String> apiChannelIds = parseChannel(wuliChannel);
		
		String channelId = DEFAULT_CHANNEL;
		if (apiChannelIds != null && apiChannelIds.size() > 0) {
			channelId = apiChannelIds.get(0);
		}
		
		getChannelList("3", advParam.getIp());// 获取频道列表 现在选择默认推荐1852
		WangYiYunResultBaseDto<WangYiYunListDataDto> resultNewList = getNewList("3", channelId, advParam.getUdid(), advParam.getIp());
		if (!resultNewList.getCode().equals(WangYiYunErrorCode.SUCCESS.getCode())) {
			log.error(gson.toJson(resultNewList));
			throw new Exception(gson.toJson(resultNewList));
		}
		AnalysisLog
		.info("[GET_WANGYI_ARTICLES]: accountId={}, udid={}, cityId={}, s={}, v={}, lineId={}, nw={},ip={},deviceType={},geo_lng={},geo_lat={},stats_act={},channelId={},refer={}",
				advParam.getAccountId(), advParam.getUdid(), advParam.getCityId(), advParam.getS(),
				advParam.getV(), advParam.getLineId(), advParam.getNw(), advParam.getIp(),
				advParam.getDeviceType(), advParam.getLng(), advParam.getLat(), advParam.getStatsAct(), 1 ,advParam.getRefer());
		List<FlowContent> flowContentList = new ArrayList<>();
		for (WangYiYunListModel model : resultNewList.getData().getInfos()) {
			// 去掉部分视频、和图集
			Random r = new Random();
			if(model.getInfoType().equals("video")) {
				if(r.nextInt(3) < 2) // 1/3的概率留下视频
					continue;
			} else if (model.getInfoType().equals("picset")) {
				if(r.nextInt(2) < 1) // 1/2的概率留下图集
					continue;
			}
			
			Map<String, String> params = New.hashMap();
			params.put("fss", "1");
//			params.put("st", model.getSource());
			params.put("st", model.getTitle());
			params.put("ak", appkey);
			params.put("sk", secretkey);
			params.put("id", model.getInfoId());
			params.put("it", model.getInfoType());
			params.put("p", "recommendation");
			params.put("rid", model.getRecId());
			params.put("info", model.getAlgInfo());
			params.put("ff", "1");
			params.put("ut", "userId");
			params.put("uk", advParam.getUdid());
			params.put("cid", model.getChannelId());
			
			// 以下为获取文章详情页内容，自己编写html文件内容。最新要求，无需这样做
//			if (!model.getInfoType().equals(WangYiYunInfoType.ARTICLE.getWangYiType())) {
//				log.info("网易：获取不到是article类型的文章，infoType={}", model.getInfoType());
//				continue;
//			}
//			WangYiYunResultBaseDto<WangYiYunDetailModel> resultDetailResult = getNewDetail("3", model.getInfoId(), advParam.getIp());
//			if (!resultDetailResult.getCode().equals(WangYiYunErrorCode.SUCCESS.getCode())) {
//				log.info(gson.toJson(resultNewList));
//				throw new Exception(gson.toJson(resultNewList));
//			}
//			WangYiYunDetailModel wyDetailModel = resultDetailResult.getData();
//			
			// 判断该文章之前是否缓存过
//			FlowContent flowContent;
//			if(! Constants.ISTEST)
//				if ((flowContent = hasCached(model)) != null) {
//					flowContentList.add(flowContent);
//					continue;
//				}
//			
//			// 写html文件
//			String date = DateUtil.getTodayStr("yyyy-MM-dd");
//			File path = new File(cdnPath + date);
//            if (!path.exists()) {
//                boolean createFlag = path.mkdirs();
//                log.info("网易：create filePath {} : {}", path, createFlag);
//            }
//			
//			File des = new File(path + "/" + wyDetailModel.getInfoId() + ".html");
//			String html = formatContentAndInstallHtml(modelFileName, wyDetailModel);
////			System.out.println("###################### " + html);
//			html = html.replaceAll("&lt;", "<");
//			html = html.replaceAll("&gt;", ">");
//			FileUtils.writeStringToFile(des, html, "utf-8");

			flowContentList.add(parseNewDetailModelToFlowContent(model, params,advParam.getUdid()));
//			flowContentList.add(parseNewDetailModelToFlowContent(wyDetailModel, newUrl));
		}
		log.info("网易：获取到文章数目: {}", flowContentList.size());
//		log.info("从api获取到的content结构是：{}", JSONObject.toJSONString(flowContentList));
		return flowContentList;
	}

	private FlowContent hasCached(WangYiYunListModel wyDetailModel) {
		FlowContent flowContent = new FlowContent();
		String flowCachedKey = AdvCache.getWangyiArticleCacheKey(wyDetailModel.getInfoId());
		String flowStr = (String) CacheUtil.get(flowCachedKey);
		if(flowStr == null) {
			return null;
		}
		flowContent = JSONObject.parseObject(flowStr, FlowContent.class);
		log.info("网易：获取到缓存过的文章，id={}", flowContent.getId());
		return flowContent;
	}

	private String formatContentAndInstallHtml(String modelHtml, WangYiYunDetailModel wyDetailModel)
			throws IOException {
		formatImagInContent(wyDetailModel);
		String html = installNewDetailHtml(modelHtml, wyDetailModel);
		return html;
	}

	private FlowContent parseNewDetailModelToFlowContent(WangYiYunListModel model, Map<String, String> params, String udid) {
//		System.out.println(url);
		FlowContent flowContent = new FlowContent();
		flowContent.setType(0);
		flowContent.setTitle(model.getTitle());
		flowContent.setUrl(createWyUrl(params, udid));
		flowContent.setId(model.getInfoId());
		flowContent.setTime(DateUtil.getDate(model.getUpdateTime(), "yyyy-MM-dd HH:mm:ss").getTime());  // 时间问题
		flowContent.setImgs(model.createImgs(model.getThumbnails()));
		flowContent.setDesc(model.getSource());

//		String flowCachedKey = AdvCache.getWangyiArticleCacheKey(flowContent.getId());
//		log.info("网易：缓存文章,id={}", flowContent.getId());
//		CacheUtil.set(flowCachedKey, Constants.SEVEN_DAY_TIME, JSONObject.toJSONString(flowContent));
		
		return flowContent;
	}

	private String createWyUrl(Map<String, String> params, String udid) {
		StringBuilder url = new StringBuilder(wangyiArticleHost);
		for(Entry<String, String> entry : params.entrySet()) {
			url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		HashMap<String, String> paramsMap = New.hashMap();
		paramsMap.put("gse", "1");		//放在最外层的参数，未编码进link参数里面，如果跳转是ad，最终页面会丢失这个参数
		paramsMap.put("wse", "1");	
//		return url.toString();
		return AdvUtil.buildRedirectLink(url.toString(), paramsMap, udid, null,
				null, false, true, 0);
	}

	@Override
	public List<String> parseChannel(FlowChannel ucChannel) {
		if (ucChannel == null) {
			return null;
		}
		List<String> ids = new ArrayList<String>();
		for (String id : ucChannel.getChannelId().split("&")) {
				ids.add(id);
		}
		return ids;
	}

	@Override
	public List<FlowContent> parseResponse(AdvParam advParam, long ftime, String recoid, String token, String channelId,
			boolean isShowAd) {
		throw new UnsupportedOperationException();
	}

	private WangYiYunResultBaseDto<WangYiYunDetailModel> getNewDetail(String platform, String infoId, String ip) {
		Set<WangYIParamForSignature> paramSet = installNewDetailParam(platform, System.currentTimeMillis(), infoId,
				"recommendation");
		WangYiYunResultBaseDto<WangYiYunDetailModel> result = getWangYiYunResponse(wangYuYunNewDetailUrl, paramSet,
				new TypeToken<WangYiYunResultBaseDto<WangYiYunDetailModel>>() {
				}.getType(), ip);
//		System.out.println(gsonFormat.toJson(result));
		if(Constants.ISTEST)
			log.info(gson.toJson(result));
		return result;
	}

	private WangYiYunResultBaseDto<WangYiYunListDataDto> getNewList(String plateForm, String channelId, String udid, String ip) {
		Set<WangYIParamForSignature> paramSet = installNewList(plateForm, System.currentTimeMillis(), channelId, udid);
		WangYiYunResultBaseDto<WangYiYunListDataDto> result = getWangYiYunResponse(wangYiYunNewListUrl, paramSet,
				new TypeToken<WangYiYunResultBaseDto<WangYiYunListDataDto>>() {
				}.getType(), ip);
//		System.out.println(gsonFormat.toJson(result));
		if(Constants.ISTEST)
			log.info(gson.toJson(result));
		return result;
	}

	private WangYiYunResultBaseDto<WangYiYunChannelDto> getChannelList(String plateForm, String ip) {
		Set<WangYIParamForSignature> paramSet = initBaseParam(plateForm, System.currentTimeMillis());
		WangYiYunResultBaseDto<WangYiYunChannelDto> result = getWangYiYunResponse(wangYiYunChannelListUrl, paramSet,
				new TypeToken<WangYiYunResultBaseDto<WangYiYunChannelDto>>() {
				}.getType(), ip);
//		System.out.println(gsonFormat.toJson(result));
//		log.info(gson.toJson(result));
		return result;

	}
	
	public static void main(String[] args) {
		WangYiYunHelp yun = new WangYiYunHelp();
		WangYiYunResultBaseDto<WangYiYunListDataDto> resultNewList = yun.getNewList("3", DEFAULT_CHANNEL, "123", "123.12.12.1");
		if (!resultNewList.getCode().equals(WangYiYunErrorCode.SUCCESS.getCode())) {
		}
		System.out.println(gson.toJson(resultNewList));
		
	}

}
