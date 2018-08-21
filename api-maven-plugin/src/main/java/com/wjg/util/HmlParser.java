package com.wjg.util;

import com.alibaba.fastjson.JSON;
import com.wjg.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTML转换器
 *
 * @author wjg
 */
public class HmlParser {

    /**
     * API默认模板
     */
    private final static String TEMPLATE = "/templates/api.html";


    private static void generate(InputStream in, String targetFile, Class<?>[] classes, String host) {

        if (in == null) {
            return;
        }

        Document doc = null;
        try {
            doc = Jsoup.parse(in, "utf-8", "");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (doc == null) {
            return;
        }

        int catelogCount = 1;

        //使用的时候，传一个class的一个集合过来，从Spring的IOC容器中获取
        for (Class<?> c : classes) {
            try {
                //过滤@Controller或者@RestController注解
                if (!c.isAnnotationPresent(Controller.class) & !c.isAnnotationPresent(RestController.class)) {
                    continue;
                }
                //如果没有配置模块，过滤
                if (!c.isAnnotationPresent(Domain.class)) {
                    continue;
                }
                //如果配置了ApiIgnore忽律
                if(c.isAnnotationPresent(ApiIgnore.class)){
                    continue;
                }
                //查找Controller类上面的ReqeustMapping值
                String namespace = "";
                //开始判断有没有配置RequestMapping
                if (c.isAnnotationPresent(RequestMapping.class)) {
                    Annotation a = c.getAnnotation(RequestMapping.class);
                    Method m = a.getClass().getMethod("value", null);
                    namespace = ((String[]) m.invoke(a))[0];
                }

                //可以给每个模块定制一个独立的子域名去访问，自己的注解
                String domain = null;
                String desc = null;
                if (c.isAnnotationPresent(Domain.class)) {
                    Annotation a = c.getAnnotation(Domain.class);

                    Method m = a.getClass().getMethod("value", null);
                    domain = m.invoke(a).toString();

                    Method dm = a.getClass().getMethod("desc", null);
                    desc = dm.invoke(a).toString();
                }

                doc.select(".interface").append(StringUtils.format(doc.select("#interfaceTitleItem").html(), "p" + catelogCount, catelogCount + "、" + (StringUtils.isEmpty(desc) ? "未描述模块" : desc)));
                doc.select(".content-body").append(StringUtils.format(doc.select("#catalogItem").html(), "#p" + catelogCount, catelogCount + "、" + (StringUtils.isEmpty(desc) ? "未描述模块" : desc)));


                //开始扫描Controller里面的所有的方法
                int interfaceCount = 1;
                Method[] ms = c.getDeclaredMethods();
                for (Method m : ms) {
                    //判断，看这个方法有没有配置RequestMapping，如果没有配置
                    //那这个方法肯定是请求不到的
                    if (!m.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }
                    //如果方法上配置了忽律则不生成此方法的api文档
                    if(m.isAnnotationPresent(ApiIgnore.class)){
                        continue;
                    }

                    doc.select(".interface").append(com.wjg.util.StringUtils.format(doc.select("#interfaceItem").html(), "s" + catelogCount + "-" + interfaceCount));
                    Element eleInterface = doc.select(".interface #" + "s" + catelogCount + "-" + interfaceCount).parents().first();

                    //如果有
                    //获取RequestMapping里面的值
                    Annotation mapping = m.getAnnotation(RequestMapping.class);
                    Method vm = mapping.getClass().getMethod("value", null);
                    String url = ((String[]) vm.invoke(mapping))[0];
                    //到这里为止，就可以拼装出来一个完整的url了


                    Method mm = mapping.getClass().getMethod("method", null);

                    Object mr = mm.invoke(mapping);
                    String method = null;
                    if (mr != null) {
                        if (((RequestMethod[]) mr).length > 0) {
                            method = (((RequestMethod[]) mr)[0]).toString();
                        }
                    }

                    String mdesc = null;
                    String name = null;
                    String author = null;
                    String createtime = null;
                    String baseUrl = "http://" + (null == domain || "".equals(domain) ? "" : (domain + ".")) + host + namespace;

                    Example example = null;

                    Map<String, String> paramDesc = new HashMap<String, String>();
                    List<Object[]> returnList = new ArrayList<Object[]>();
                    List<String[]> pars = new ArrayList<String[]>();

                    //获取我们自定义的API配置信息
                    if (m.isAnnotationPresent(Api.class)) {
                        Annotation a = m.getAnnotation(Api.class);

                        Method nm = a.getClass().getMethod("name", null);
                        name = nm.invoke(a).toString();

                        Method dm = a.getClass().getMethod("desc", null);
                        mdesc = dm.invoke(a).toString();

                        Method am = a.getClass().getMethod("author", null);
                        author = am.invoke(a).toString();

                        Method cm = a.getClass().getMethod("createTime", null);
                        createtime = cm.invoke(a).toString();
                        //入参
                        Method pm = a.getClass().getMethod("params", null);
                        Column[] params = (Column[]) pm.invoke(a);


                        //开始扫描方法上面参数
                        for (Column param : params) {
                            paramDesc.put(param.name(), param.desc());

                            pars.add(new String[]{
                                    param.name(),
                                    param.type(),
                                    param.maxLength(),
                                    (param.required() ? "是" : "否"),
                                    (StringUtils.isEmpty(param.desc())? "无" : param.desc())});
                        }
                        //出参
                        Method rm = a.getClass().getMethod("returns", null);
                        Column[] returns = (Column[]) rm.invoke(a);

                        for (Column r : returns) {
                            returnList.add(new Object[]{
                                    r.name(),
                                    r.type(),
                                    r.maxLength(),
                                    (r.required()?"是":"否"),
                                    (StringUtils.isEmpty(r.desc())?"无":r.desc()),
                                    r.dataRules()});
                        }

                        Method dem = a.getClass().getMethod("example", null);
                        Object o = dem.invoke(a);

                        if (o != null) {
                            example = (Example) o;
                        }
                    }

                    //往html文档里面写配置信息了
                    eleInterface.select(".title").html(catelogCount + "." + interfaceCount + "、" + (StringUtils.isEmpty(name) ? "未描述接口名称" : name));
                    eleInterface.select(".desc").html(StringUtils.isEmpty(mdesc) ? "未描述功能" : mdesc);
                    eleInterface.select(".url").html(baseUrl + url);
                    eleInterface.select(".method").html((null == method || "".equals(method) ? "GET/POST" : method.toUpperCase()));
                    eleInterface.select(".author").html(StringUtils.isEmpty(author) ? "未描述作者" : author);
                    eleInterface.select(".createtime").html(StringUtils.isEmpty(createtime) ? "未描述最后修改时间" : createtime);

                    //如果最终扫描出来结果，参数个数为0，表示访问这个接口是不需要带参数的
                    if (pars.size() == 0) {
                        eleInterface.select(".params table").html("无");
                    }

                    //如果有参数，填充到我们模板中，最终生成html
                    for (int index = 0; index < pars.size(); index++) {
                        String[] arr = pars.get(index);
                        eleInterface.select(".params table").append(
                                StringUtils.format(doc.select("#paramItem").html(),
                                        (((index + 1) % 2 == 0) ? "odd" : "eve"),
                                        arr[0],
                                        arr[1],
                                        arr[2],
                                        arr[3],
                                        arr[4]));
                    }

                    //处理返回结果说明
                    if (returnList.size() == 0) {
                        eleInterface.select(".returns table").html("");
                    }

                    for (int index = 0; index < returnList.size(); index++) {
                        Object[] arr = returnList.get(index);
                        eleInterface.select(".returns table").append(
                                StringUtils.format(doc.select("#returnItem").html(),
                                        (((index + 1) % 2 == 0) ? "odd" : "eve"),
                                        arr[0].toString(),
                                        arr[1].toString(),
                                        arr[2].toString(),
                                        arr[3].toString(),
                                        arr[4].toString()));
                        //处理数据字段
                        DataColumn[] dataColumns = (DataColumn[])arr[5];
                        for (DataColumn dataColumn : dataColumns) {
                            eleInterface.select(".returns table").append(
                                    StringUtils.format(doc.select("#returnItem").html(),
                                            (((index + 1) % 2 == 0) ? "odd" : "eve"),
                                            "&nbsp;&nbsp;&nbsp;&nbsp;└ "+ dataColumn.name(),
                                            dataColumn.type(),
                                            dataColumn.maxLength(),
                                            dataColumn.required(),
                                            dataColumn.desc()));
                        }
                    }
                    //处理例子
                    if (example != null) {
                        //添加返回说明
                        eleInterface.select(".returns").append(doc.select("#demoDetailItem").html());
                        if (!StringUtils.isEmpty(example.param())) {
                            try{
                                String json = JSON.toJSONString(JSON.parse(example.param()), true);
                                json = json.replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
                                eleInterface.select(".item-bd").append(com.wjg.util.StringUtils.format(doc.select("#demoItem").html(), json));
                            }catch (Exception e){
                                eleInterface.select(".item-bd").append(com.wjg.util.StringUtils.format(doc.select("#demoItem").html(), example.param()));
                            }

                        }
                        if (!StringUtils.isEmpty(example.success())) {
                            String json = example.success();
                            try {
                                json = JSON.toJSONString(JSON.parse(json), true);
                                json = json.replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
                            } catch (Exception e) {

                            }
                            eleInterface.select(".item-bd").append(com.wjg.util.StringUtils.format(doc.select("#successItem").html(), json));
                        }
                        if (!StringUtils.isEmpty(example.error())) {
                            String json = example.error();
                            try {
                                json = JSON.toJSONString(JSON.parse(json), true);
                                json = json.replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            eleInterface.select(".item-bd").append(com.wjg.util.StringUtils.format(doc.select("#errorItem").html(), json));
                        }
                    }


                    interfaceCount++;
                }

                doc.select(".interface").append("<div style='clear:both;margin-bottom:20px;'></div>");
                catelogCount++;

            } catch (Exception e) {
                continue;
            }


            //输出到指定目录
            try {
                OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(new File(targetFile)), "utf-8");
                ow.write(doc.html());
                ow.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 生成HTML文档
     *
     * @param templateFile HTML模板文件
     * @param targetFile   生成目标文件
     * @param classes      需要分析的class
     * @param host         url域名
     */
    public static void generate(String templateFile, String targetFile, List<Class> classes, String host) {
        Class[] c = new Class[classes.size()];
        generate(templateFile, targetFile, classes.toArray(c), host);
    }

    /**
     * 生成HTML文档
     *
     * @param templateFile HTML模板文件
     * @param targetFile   生成目标文件
     * @param classes      需要分析的class
     * @param host         url域名
     */
    public static void generate(String templateFile, String targetFile, Class[] classes, String host) {
        InputStream in = null;
        try {
            in = new FileInputStream(new File(templateFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        generate(in, targetFile, classes, host);
    }

    /**
     * 生成HTML文档
     *
     * @param targetFile 生成目标文件
     * @param clazz      需要分析的class
     * @param host       url域名
     */
    public static void generate(String targetFile, List<Class<?>> clazz, String host) {
        Class<?>[] c = new Class[clazz.size()];
        generate(targetFile, clazz.toArray(c), host);
    }


    /**
     * 生成HTML文档
     *
     * @param targetFile 生成目标文件
     * @param classes    需要分析的class
     * @param host       url域名
     */
    private static void generate(String targetFile, Class[] classes, String host) {
        InputStream in = null;
        try {
            in = HmlParser.class.getResourceAsStream(TEMPLATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        generate(in, targetFile, classes, host);
    }

}
