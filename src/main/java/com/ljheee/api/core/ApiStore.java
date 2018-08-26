package com.ljheee.api.core;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lijianhua04 on 2018/7/26.
 */
@Service
public class ApiStore {

    private ApplicationContext applicationContext;

    private HashMap<String, ApiRunnable> apiMap = new HashMap<String, ApiRunnable>();


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApiStore(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public void loadApiFromSpringBeans() {
        String[] names = applicationContext.getBeanDefinitionNames();
        Class<?> type;

        for (String name : names) {
            type = applicationContext.getType(name);

            for (Method m : type.getDeclaredMethods()) {
                ApiMapping annotation = m.getAnnotation(ApiMapping.class);
                if (annotation != null) {
                    addApiItem(annotation, name, m);
                }
            }
        }
    }

    public ApiRunnable findApiRunnable(String apiName) {
        return apiMap.get(apiName);
    }


    public List<ApiRunnable> findApiRunnables(String apiName) {

        if(apiName ==null){
            throw new IllegalArgumentException("apiName must not null");
        }
        List<ApiRunnable> list = new ArrayList<ApiRunnable>(20);
        for (ApiRunnable api : apiMap.values()) {
            if (api.apiName.equals(apiName)) {
                list.add(api);
            }
        }
        return list;
    }


    private void addApiItem(ApiMapping apiMapping, String beanName, Method m) {

        ApiRunnable apiRunnable = new ApiRunnable();

        apiRunnable.apiName = apiMapping.value();
        apiRunnable.targetMethod = m;
        apiRunnable.targetName = beanName;
        apiMap.put(apiMapping.value(), apiRunnable);
    }

    public boolean containsApi(String apiName) {
        return apiMap.containsKey(apiName);
    }


    public class ApiRunnable {

        String apiName; // com.ljheee.api.service.adduser
        String targetName;// ioc bean

        Object target;// serviceImpl 实例
        Method targetMethod;


        public Object run(Object... args) throws InvocationTargetException, IllegalAccessException {

            if (target == null) {
                target = applicationContext.getBean(targetName);
            }
            return targetMethod.invoke(target, args);
        }

        public Class<?>[] getParamTypes() {
            return targetMethod.getParameterTypes();
        }

    }


}
