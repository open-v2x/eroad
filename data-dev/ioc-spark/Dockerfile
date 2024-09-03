# 使用 OpenJDK 作为基础镜像
FROM openjdk:11

# 设置工作目录
WORKDIR /app

# 将应用程序的 JAR 文件添加到镜像中
COPY ioc-spark-1.0-SNAPSHOT.jar /app/ioc-spark-1.0-SNAPSHOT.jar

# 运行应用程序，指定主类
CMD ["java", "-cp", "ioc-spark-1.0-SNAPSHOT.jar", "cn.eroad.app.ioc.Ads_Event_Count"]
