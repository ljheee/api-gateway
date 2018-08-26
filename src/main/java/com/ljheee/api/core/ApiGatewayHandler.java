package com.ljheee.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ljheee.api.service.Goods;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by lijianhua04 on 2018/7/27.
 */
@Service
public class ApiGatewayHandler implements ApplicationContextAware, InitializingBean {

    private static final String METHOD = "method";
    private static final String PARAMS = "params";

    private ApiStore apiStore;

    final ParameterNameDiscoverer parameterUtil;

    public ApiGatewayHandler() {
        this.parameterUtil = new LocalVariableTableParameterNameDiscoverer();//ASM
    }

    public void afterPropertiesSet() throws Exception {
        apiStore.loadApiFromSpringBeans();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        apiStore = new ApiStore(applicationContext);
    }

    // OK
    public void handle(HttpServletRequest request, HttpServletResponse response) {


        String params = request.getParameter(PARAMS);
        String method = request.getParameter(METHOD);


        Object result = null;
        ApiStore.ApiRunnable apiRunnable = null;

        try {
            // 系统 参数校验
            apiRunnable = sysParamsValdate(request);

            // 构建 业务参数
            Object[] args = buildParams(apiRunnable, params, request, response);

            // 反射执行
            result = apiRunnable.run(args);

        } catch (ApiException e) {
            response.setStatus(500);
            result = handleError(e);
        } catch (InvocationTargetException e) {
            response.setStatus(500);
            result = handleError(e.getTargetException());
        } catch (Exception e) {
            response.setStatus(500);
            result = handleError(e);
        }

        returnResult(result, response);
    }

    // ok
    private Object handleError(Throwable throwable) {

        String code = "";
        String msg = "";

        if (throwable instanceof ApiException) {
            code = "0001";
            msg = throwable.getMessage();
        } else {
            code = "0002";
            msg = throwable.getMessage();
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("error", code);
        result.put("msg", msg);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        throwable.printStackTrace(printStream);
        return result;
    }

    // ok
    private Object[] buildParams(ApiStore.ApiRunnable apiRunnable, String params, HttpServletRequest request, HttpServletResponse response) throws ApiException {

        Map<String, Object> map = null;

        try {
            map = JsonUtil.toMap(params);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ApiException("调用失败：json格式异常，请检查params参数。ExceptionMessage=" + e.getMessage());
        } finally {
            System.out.println(map);
        }

        if (map == null) {
            map = new HashMap<String, Object>();
        }

        Method method = apiRunnable.targetMethod;
        List<String> paramNames = Arrays.asList(parameterUtil.getParameterNames(method));

        Class<?>[] paramTypes = method.getParameterTypes();

        for (Map.Entry<String, Object> m : map.entrySet()) {
            if (!paramNames.contains(m.getKey())) {
                throw new ApiException("调用失败：接口不存在" + m.getKey() + "参数");
            }
        }

        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                args[i] = request;
            } else if (map.containsKey(paramNames.get(i))) {
                try {
                    args[i] = convertJson2Bean(map.get(paramNames.get(i)), paramTypes[i]);
                } catch (IOException e) {
                    throw new ApiException("调用失败，指定参数格式错误或值错误" + paramNames.get(i) + "e=" + e.getMessage());
                }
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    // 系统参数  校验 OK
    private ApiStore.ApiRunnable sysParamsValdate(HttpServletRequest request) throws Exception {

        // 校验 系统参数 method和params
        String apiName = request.getParameter(METHOD);
        String json = request.getParameter(PARAMS);

        ApiStore.ApiRunnable api;
        if (apiName == null || apiName.trim().equals("")) {
            throw new ApiException("调用失败，参数method为空");
        } else if (json == null) {
            throw new ApiException("调用失败，参数params为空");
        } else if ((api = apiStore.findApiRunnable(apiName)) == null) {
            throw new ApiException("调用失败，指定API不存在，API=" + apiName);
        }
        return api;
    }


    //将map 转化为具体的 目标方法 参数对象 ok
    private <T> Object convertJson2Bean(Object val, Class<T> targetClass) throws IOException {
        Object result = null;
        if (val == null) {
            return null;
        } else if (Integer.class.equals(targetClass)) {
            result = Integer.parseInt(val.toString());
        } else if (Long.class.equals(targetClass)) {
            result = Long.parseLong(val.toString());
        } else if (Date.class.equals(targetClass)) {

            if (val.toString().matches("[0-9]+")) {
                result = new Date(Long.parseLong(val.toString()));
            } else {
                throw new IllegalArgumentException("日期必须是长整型的时间戳");
            }

        } else if (String.class.equals(targetClass)) {
            if (val instanceof String) {
                result = val;
            } else {
                throw new IllegalArgumentException("转换目标类型为字符串");
            }
        } else {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
//            result = mapper.readValue(String.valueOf(val), targetClass);
            result = JsonUtil.convertValue(val, targetClass);// TODO

        }

        return result;
    }


    //ok
    private void returnResult(Object result, HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
            String json = mapper.writeValueAsString(result);

            response.setCharacterEncoding("UTF-8");

            response.setContentType("text/heml/json;charset=utf-8");
            response.setHeader("Pargma", json);
            response.setDateHeader("Expires", 0);
            if (json != null) {
                response.getWriter().write(json);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException("服务中心响应异常");
        }
    }

    public static void main(String[] args) throws JsonProcessingException {

        String mapString = "{\"goods\":{\"code\":\"test_code\",\"size\":12},\"id\":19}";
        Map<String, Object> map = JsonUtil.toMap(mapString);
        System.out.println(map);

        Object str = "{\"code\":\"test_code\",\"size\":12}";

        Goods g = JsonUtil.convertValue(str, Goods.class);

//        Goods goods = new Goods("test_code",12);
//        str = new ObjectMapper().writeValueAsString(goods);

        System.out.println(g);



        Integer[] id = new Integer[3];
        id[0] = 1;
        id[1] = 2;
        id[2] = 3;
        System.out.println(new ObjectMapper().writeValueAsString(id));
        String srtId = "[1,2,3]";

        Integer[] arr = JsonUtil.convertValue(srtId, Integer[].class);
        System.out.println(Arrays.toString(arr));//  [1, 2, 3]

        String mString = "";


    }


}
