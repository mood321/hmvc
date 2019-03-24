package com.he.servlet;

import com.he.annaotation.*;
import com.he.controller.TestController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    //class 集合
    List<String> className = new ArrayList<String>();

    //向mvc注册的bean
    Map<String, Object> objectMap = new HashMap<String, Object>();

    //用于url定位 方法的map
    Map<String, Object> urlmap = new HashMap<String, Object>();

    private static final long serialVersionUID = -9056479076286804737L;


    public void init(ServletConfig config) {
        // IOC bean 扫描 所有class
        scanPackage("com.he");
        //实例化所有有注解的bean
        doInstance();

        //注入
        doIoc();
        //通过url定位方法 /mood/query
        buildUrlMapping();
    }

    private void buildUrlMapping() {
        if (objectMap.entrySet().size() <= 0) {
            System.out.println("DispatcherServlet.buildUrlMapping 无实例化bean ");
            return;
        }
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Object intance = entry.getValue();
            Class<?> clazz = intance.getClass();
            if (clazz.isAnnotationPresent(MoodController.class)) {
                MoodRequestMapping requestMapping = clazz.getAnnotation(MoodRequestMapping.class);
                String classPath = requestMapping.value();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(MoodRequestMapping.class)) {
                        MoodRequestMapping methodPath = method.getAnnotation(MoodRequestMapping.class);
                        String methodPa = methodPath.value();
                        urlmap.put(classPath + methodPa, method);
                    } else {
                        continue;
                    }
                }

            } else {
                continue;
            }
        }

    }

    /**
     * ioc 注入
     */
    private void doIoc() {

        if (objectMap.entrySet().size() <= 0) {
            System.out.println("DispatcherServlet.doIoc map为0");
            return;
        }

        //取出注册的bean
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz =
                    instance.getClass();
            //可判断 是否只注入 controller或 service
            Field[] fields = clazz.getDeclaredFields();
            for (Field filed : fields) {
                if (filed.isAnnotationPresent(MoodAutowired.class)) {

                    MoodAutowired moodAutowired = filed.getAnnotation(MoodAutowired.class);
                    String key = moodAutowired.value();
                    //私有属性 需要设置才能注入
                    filed.setAccessible(true);
                    try {
                        //属性注入
                        filed.set(instance, objectMap.get(key));

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                } else {
                    continue;
                }


            }
        }
    }

    /**
     * 扫描位置 扫描
     *
     * @param basePackage
     */
    private void scanPackage(String basePackage) {

        URL url = this.getClass().getClassLoader().getResource("/" +basePackage.replaceAll("\\.", "/"));
        String fileStr = url.getFile();
        String[] fileList = new File(fileStr).list();
        for (String path : fileList) {
            File file = new File(fileStr  +"/"+ path);
            if (file.isDirectory()) {
                scanPackage(basePackage  +"."+ path);
            } else {
                className.add(basePackage + "." + file.getName());


            }

        }
    }

    /**
     * 实例化list全类名
     */
    private void doInstance() {
        if (className.size() <= 0) {
            System.out.println("扫描失败！");
            return;
        }
        //处理list的class
        for (String classN : className) {
            String cn = classN.replaceAll("\\.class", "");
            try {
                Class<?> clazz = Class.forName(cn);
                if (clazz.isAnnotationPresent(MoodController.class)) {
                    Object instace = clazz.newInstance();
                    MoodRequestMapping annotation = clazz.getAnnotation(MoodRequestMapping.class);
                    String rmvalue = annotation.value();
                    objectMap.put(rmvalue, instace);
                } else if (clazz.isAnnotationPresent(MoodService.class)) {
                    Object instace = clazz.newInstance();
                    MoodService annotation = clazz.getAnnotation(MoodService.class);
                    String rmvalue = annotation.value();
                    objectMap.put(rmvalue, instace);
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //请求路径
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.replace(contextPath, "");
        Method method = (Method) urlmap.get(path);
        //去map寻找
      /*  String[] split = path.split("/");
        String s =split [1];*/
        TestController instancs = (TestController) objectMap.get("/" + path.split("/")[1]);//mood

        //参数
        Object[] arg = hand(resp,req, method);

        try {
            method.invoke(instancs,arg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Object[] hand(HttpServletResponse res, HttpServletRequest req, Method method) {
        Class<?>[] paramClazz = method.getParameterTypes();
        Object[] args = new Object[paramClazz.length];
        int args_i = 0;
        int index = 0;
        for (Class<?> paramClass : paramClazz) {
            if (ServletRequest.class.isAssignableFrom(paramClass)) {
                args[args_i++] = req;
            }
            ;
            if (ServletResponse.class.isAssignableFrom(paramClass)) {
                args[args_i++] = res;
            }

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Annotation[] parames= parameterAnnotations[index];

            if(parames.length>0){
                for (Annotation paramAn:parames){
                    if(MoodRequestParam.class.isAssignableFrom(paramAn.getClass())){

                        //方法参数
                        if(paramClass.getName().equals("java.lang.Integer")){
                            MoodRequestParam rq=(MoodRequestParam) paramAn;
                            args[args_i++] =Integer.parseInt(req.getParameter(rq.value()));
                        }
                        if(paramClass.getName().equals("java.lang.String")){
                            MoodRequestParam rq=(MoodRequestParam) paramAn;
                            args[args_i++] =req.getParameter(rq.value());
                        }

                    }
                }
            }
            index++;
        }

        return args;

    }

}
