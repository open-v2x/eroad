package cn.eroad.core.utils;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.Head;

/**
 * 能力中台工具类
 *
 * @date 2021/10/25 10:34
 */
public class OcToolsUtil {
    public static boolean isSuccess(CommonContent response) {
        try {
            if (response == null || response.getHead() == null) {
                return false;
            }
            Head head = response.getHead();
            return "S".equals(head.getStatus()) && "000000".equals(head.getCode());
        } catch (Exception e) {
            return false;
        }
    }
}
