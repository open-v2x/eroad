package cn.eroad;




public class ConnProp {
    public final static String STORAGE_KAFKA_SERVER = System.getenv("STORAGE_KAFKA_SERVER");
    public final static String IOC_MYSQL_URL = System.getenv("IOC_MYSQL_URL");
    public final static String IOC_MYSQL_USR = System.getenv("IOC_MYSQL_USR");
    public final static String IOC_MYSQL_PSW = System.getenv("IOC_MYSQL_PSW");

    private ConnProp(){}
}
