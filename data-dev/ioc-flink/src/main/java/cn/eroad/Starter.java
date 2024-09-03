package cn.eroad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static cn.eroad.ConnProp.*;
import static cn.eroad.ConnProp.IOC_MYSQL_PSW;

public class Starter {
    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws Exception {
        System.out.println("ConnProp:"+STORAGE_KAFKA_SERVER+" "+IOC_MYSQL_URL+" "+IOC_MYSQL_USR+" "+IOC_MYSQL_PSW);
        String className = "cn.eroad.app.OnRoadTargetsCnt";
        String[] classArgs = {"arg1", "arg2"};

        LOG.info("Classname is " + className);
        try {
            Class<?> aClass = Class.forName(className);
            Object o = aClass.getDeclaredConstructor().newInstance();
            Method flinkBegin = aClass.getMethod("flinkBegin", String[].class);
            LOG.info(">>>>>>>>>>>>>>>>>>START USER CODE<<<<<<<<<<<<<<<<<<<<");
            flinkBegin.invoke(o, (Object) classArgs);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(">>>指定类不存在,请检查类名<<<", e);
        }
    }
}
