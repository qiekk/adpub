package com.bus.chelaile.mvc.rule;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bus.chelaile.model.ads.entity.TasksGroup;
import com.bus.chelaile.mvc.AbstractController;
import com.bus.chelaile.mvc.AdvParam;
import com.bus.chelaile.mvc.BusAdvActionV2;
import com.bus.chelaile.service.StaticAds;
import com.bus.chelaile.util.New;


@Controller
@RequestMapping("/js/android/js/rule")
public class JsRule extends AbstractController {
    
    private static final Logger logger = LoggerFactory.getLogger(JsRule.class);

    @ResponseBody
    @RequestMapping("/splash.js")
    public String splash(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        return "hello";
    }

    @ResponseBody
    @RequestMapping("/splashAd.js")
    public String splashAd(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        AdvParam p = getActionParam(request);
        logger.info("***请求splashAds.js, s={}, v={}, vc={}, udid={}, cityId={}", p.getS(), p.getV(), p.getVc(), p.getUdid(), p.getCityId());

        // 模板 
        String splashOrigin = StaticAds.JS_FILE_STR.get("splash_origin.js");
        //        TasksGroup tasksGroups = JSService.getTask("splash");
        TasksGroup tgs = new TasksGroup();
        List<String> task1 = New.arrayList();
        task1.add("api_voicead");
        task1.add("sdk_toutiao");

        List<String> task2 = New.arrayList();
        task2.add("sdk_baidu");

        List<List<String>> ts = new ArrayList<List<String>>();
        ts.add(task1);
        ts.add(task2);

        List<Long> times = New.arrayList();
        times.add(200L);
        times.add(1500L);

        tgs.setTasks(ts);
        tgs.setTimeouts(times);

        String splashJS = splashOrigin.replace("${TIMEOUTS}", tgs.getTasks().toString());
        splashJS = splashOrigin.replace("${TASKS}", tgs.getTimeouts().toString());
        
//        return "hello, splashAd";
        return splashJS;
    }

}
