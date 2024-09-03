package cn.eroad.core.utils;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/25
 * @description
 */
public class StringOperator {

    private StringOperator() {

    }

    /**
     * 将内容以分隔符进行连接
     *
     * @param prefix  前缀
     * @param gap     间隔符
     * @param objects 连接内容
     * @return
     */
    public static String join(String prefix, String gap, Object... objects) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        builder.append(gap);
        for (int i = 0; i < objects.length; i++) {
            builder.append(objects[i]);
            if (i < objects.length - 1) {
                builder.append(gap);
            }
        }
        return builder.toString();
    }


}
