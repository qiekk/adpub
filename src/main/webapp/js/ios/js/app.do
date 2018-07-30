"use strict";var _typeof2="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(t){return typeof t}:function(t){return t&&"function"==typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t};String.prototype.contains=String.prototype.contains||function(t){return 0<=this.indexOf(t)},String.prototype.startsWith=String.prototype.startsWith||function(t){return 0===this.indexOf(t)},String.prototype.endsWith=String.prototype.endsWith||function(t){return 0<=this.indexOf(t,this.length-t.length)},function(s){var o={},d=s.require;s.require=function(t,e,a){try{var r,n=t;return n.startsWith("./")&&(n=n.slice(2)),n.endsWith(".do")||(n+=".do"),e||a?r=d.apply(this,arguments):"function"!=typeof(r=o[n])&&(r=d.apply(this,arguments),o[n]=r),r?r(s):function(){}}catch(t){throw console.log("e="+t),delete o[n],t}},s.AddModule=function(t,e){o[t]=e}}(global),global.AddModule("event.do",function(t){var h={};return t=t||this,exports={},h.exports=exports,function(t,e,a){var n="https://atrace.chelaile.net.cn/thirdSimple",o="https://atrace.chelaile.net.cn/thirdPartyResponse";function d(t,e,a){var r=function(t,e){var a="",r="",n="";if(0<=t.indexOf("#")){var s=t.split("#");a=0<=s[0].indexOf("?")?s[0]+"&":s[0]+"?",r="#"+s.slice(1).join("#")}else a=0<=t.indexOf("?")?t+"&":t+"?";for(var o in e)n+="&"+o+"="+encodeURIComponent(e[o]);return a+n+r}(t,e);null!=a&&null!=a&&"string"!=typeof a&&(a=JSON.stringify(a)),a?Http.post(r,null,a,5e3,function(t,e,a){console.log("sendTrackRequest ret="+t+" response.header="+JSON.stringify(OCValueForKey(e,"allHeaderFields"))+" error="+a)}):Http.get(r,null,5e3,function(t,e,a){console.log("sendTrackRequest ret="+t+" response.header="+JSON.stringify(OCValueForKey(e,"allHeaderFields"))+" error="+a)})}function i(t,e,a){null!=a&&null!=a&&(t[e]=a)}function s(t,e){var a={},r=e.info||{},n=t.traceInfo||{};return i(a,"traceid",n.traceid),i(a,"pid",n.pid),i(a,"aid",t.aid),i(a,"ad_order",r.ad_order),i(a,"adid",r.adid),i(a,"startMode",r.startMode),i(a,"stats_act",r.stats_act),i(a,"viewstatus",e.viewstatus),a}function r(t,e){var a=s(t,e),r=e.info||{};t.traceInfo;i(a,"show_status",r.show_status||0),i(a,"cost_time",r.cost_time),i(a,"is_backup",r.is_backup),i(a,"adv_title",r.head),i(a,"adv_desc",r.subhead),d("https://atrace.chelaile.net.cn/exhibit",a)}function l(t,e){var a=s(t,e),r=e.info||{};t.traceInfo;i(a,"adv_title",r.head),i(a,"adv_desc",r.subhead),d("https://atrace.chelaile.net.cn/click",a)}function u(t,e){d("https://atrace.chelaile.net.cn/close",s(t,e))}function c(t){var e={},a=(t=t||{}).task||{},r=t.rule||{},n=t.userdata||{};return r.traceInfo&&(i(e,"traceid",r.traceInfo.traceid),i(e,"pid",r.traceInfo.pid)),a.aid&&i(e,"aid",a.aid()),i(e,"req_timestamp",+new Date),i(e,"eventId",n.uniReqId),e}function p(t){var e=c(t),a=((t=t||{}).task,t.rule,t.data||{}),r=function(t){if(t&&t.adEntityArray&&0<t.adEntityArray.length)return t.adEntityArray[0].info}(a),n=r||{};i(e,"ad_order",n.ad_order),i(e,"adid",n.adid),i(e,"req_time",(a.sdk&&a.sdk.didReqTime||0)-(a.sdk&&a.sdk.willReqTime||0)),i(e,"ad_status",r?1:0),i(e,"resp_size",a.contentLength),t.error?i(e,"code",t.error):i(e,"code",OCValueForKey(a.extensionData,"statusCode"));var s={};i(s,"adv_title",n.head),i(s,"adv_desc",n.subhead),i(s,"icon_image",n.pic),i(s,"main_image",n.picsList),i(s,"link",n.link),i(s,"url_type",n.adType),d(o,e,s)}function f(t,e,a){var r;console.log("trackEvent eventId="+t+" eventType="+e+" params="+JSON.stringify(a||{})),e==TrackClass.Type.LoadSplash||e==TrackClass.Type.LoadBanner?(r=c(a),d(n,r)):e==TrackClass.Type.LoadedSplash||e==TrackClass.Type.LoadedBanner?p(a):e!=TrackClass.Type.FailedSplash&&e!=TrackClass.Type.FailedBanner||p(a)}a.trackEvent=f,h.exports={trackExhibit:r,trackClick:l,trackClose:u,trackEvent:f},a.TrackClass={trackExhibit:r,trackClick:l,trackClose:u,trackEvent:f,Type:{LoadSplash:"LoadSplash",LoadBanner:"LoadBanner",LoadedSplash:"LoadedSplash",LoadedBanner:"LoadedBanner",FailedSplash:"FailedSplash",FailedBanner:"FailedBanner",AllAdTimeout:"AllAdTimeout",FetchedAd:"FetchedAd",NoDataLastGroup:"NoDataLastGroup"}}}(0,exports,t),h.exports}),global.AddModule("fetch.do",function(t){var o={};return t=t||this,exports={},o.exports=exports,function(t,e,a){var n="function"==typeof Symbol&&"symbol"===_typeof2(Symbol.iterator)?function(t){return void 0===t?"undefined":_typeof2(t)}:function(t){return t&&"function"==typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":void 0===t?"undefined":_typeof2(t)};var r={};function s(t){var a=[];return(t=t||[]).forEach(function(t){var e=function(t){var e=r["sdk"];if(!e){try{e=require("sdks/"+"sdk")}catch(t){console.log(t)}e&&(r.sdk=e)}return e}(t.sdkname());e&&a.push({task:t,sdk:e})}),a}var p=1e3,f=2e3;function h(){return(new Date).getTime()}o.exports=function(e,t,a){if(!e)return a(null);Array.isArray(e.timeouts)&&(p=e.timeouts[0]||p,f=e.timeouts[1]||f);var r=function(t){t&&"object"==(void 0===t?"undefined":n(t))&&t.sdk&&(t.sdk.refreshTime=25e3,t.sdk.traceInfo=e.traceInfo,t.sdk.mixRefreshAdInterval=15e3,t.sdk.maxSplashTimeout=8e3,t.sdk.warmSplashIntervalTime=12e4),a(t)};r.userdata=t,!e.tasks||e.tasks.length<=0?a(null):function e(o,a,d){var r=o.tasks;function i(t){return t?(TrackClass.trackEvent(d.userdata.uniReqId,TrackClass.Type.FetchedAd,{data:t,rule:o,userdata:d.userdata}),d(t)):a==r.length-1?(TrackClass.trackEvent(d.userdata.uniReqId,TrackClass.Type.NoDataLastGroup,{rule:o,userdata:d.userdata}),d(null)):void e(o,a+1,d)}function l(a){n&&clearInterval(n),c.forEach(function(t,e){e!==a&&t.sdk.stop2&&t.sdk.stop2(t.task)})}function t(){var t=h()-u;if(p+f<t)return TrackClass.trackEvent(d.userdata.uniReqId,TrackClass.Type.AllAdTimeout,{used:t,rule:o,userdata:d.userdata}),l(),void i(null);for(var e,a,r=0,n=0,s=0;s<c.length;s++)if(e=c[s],null!=(a=e._result)&&(r++,a[0]&&(n++,p<t||0==s)))return l(s),void i(a[0]);r>=c.length&&(0==n?(console.log("All finish without any succeed."),l(),i(null)):(l(),0<minIndex&&minIndex<c.length-1&&(e=c[minIndex],i((a=e._result)[0]))))}t._count=0;var u=h(),c=s(r[a]);c.forEach(function(e){var t=e.task.adurl_ios();console.log("try sdk: "+t.url),e.sdk.load(e.task,o,d.userdata,p+f,function(t){console.log("uniReqId="+d.userdata.uniReqId+" data comes "+t),e._result=[t]})});var n=setInterval(t,50)}(e,0,r)}}(0,exports),o.exports}),global.AddModule("sdks/sdk.do",function(t){var e={};return t=t||this,exports={},e.exports=exports,exports,e.exports={load:function(o,d,i,t,l){var e={GDTSDK:"CLLGdtSdk",BaiduSDK:"CLLBaiduSdk",TOUTIAOSDK:"CLLTTSdk",IFLYSDK:"CLLIflySdk",InMobiSdk:"CLLInMobiSdk"},r=o.adurl_ios();r.url&&0==r.url.toLowerCase().indexOf("http")?(r.data=r.data||{},r.data.placementId=r.url,r.url="CLLAdApi"):r.url&&e[r.url]&&(r.url=e[r.url]),r.url;var a=newInstance(r.url);if(!a)return l(null);if(r.type=r.type||r.pos,"splash"==r.type)TrackClass.trackEvent(i.uniReqId,TrackClass.Type.LoadSplash,{userdata:i,rule:d,task:o}),a.loadSplash(r.data,i,t,function(t){try{o.aid&&t.sdk&&(t.sdk.aid=o.aid());var e=o.filter_ios;if(e&&t&&(t.adEntityArray=e(t.adEntityArray)),i&&i.startMode&&t.adEntityArray&&0<t.adEntityArray.length){var a=t.adEntityArray[0].info;a.startMode=i.startMode,t.adEntityArray[0].info=a}TrackClass.trackEvent(i.uniReqId,TrackClass.Type.LoadedSplash,{data:t,userdata:i,rule:d,task:o}),l(t)}catch(t){TrackClass.trackEvent(i.uniReqId,TrackClass.Type.FailedSplash,{error:"-91000",des:""+t,requestInfo:r,userdata:i,rule:d,task:o}),l(null)}},function(t){t=t||"-90000",TrackClass.trackEvent(i.uniReqId,TrackClass.Type.FailedSplash,{error:t,requestInfo:r,userdata:i,rule:d,task:o}),l(null)});else if("banner"==r.type){if(TrackClass.trackEvent(i.uniReqId,TrackClass.Type.LoadBanner,{userdata:i,rule:d,task:o}),o.adStyle){var n={1:{showWidth:180,showHeight:88},2:{showWidth:96,showHeight:64},5:{showWidth:96,showHeight:64}}[o.adStyle()+""];n&&(i.showWidth=n.showWidth,i.showHeight=n.showHeight)}a.loadBanner(r.data,i,t,function(e){try{if(o.aid&&e.sdk&&(e.sdk.aid=o.aid()),e&&e.adEntityArray)for(var t=0;t<e.adEntityArray.length;t++){var a=e.adEntityArray[t],r=a.info;if(r){if(o.adStyle&&(r.displayType=o.adStyle()),r.head=r.head||"",r.subhead=r.subhead||"",r.stats_act=i.stats_act,r.head.length>r.subhead.length){var n=r.subhead;r.subhead=r.head,r.head=n}a.info=r}}var s=o.filter_ios;s&&e&&(e.adEntityArray=s(e.adEntityArray)),TrackClass.trackEvent(i.uniReqId,TrackClass.Type.LoadedBanner,{userdata:i,data:e,rule:d,task:o}),l(e)}catch(t){TrackClass.trackEvent(i.uniReqId,TrackClass.Type.FailedBanner,{error:"-91001",des:""+t,userdata:i,data:e,rule:d,task:o}),l(null)}},function(t,e,a){t=t||"-90001",TrackClass.trackEvent(i.uniReqId,TrackClass.Type.FailedBanner,{error:t,requestInfo:r,userdata:i,data:e,rule:d,task:o}),l(null)})}},stop2:function(t){t.adurl_ios().url}},e.exports}),require("./event");