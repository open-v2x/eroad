package cn.eroad.videocast.model.regist;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @Auther zhaohanqing
 * @Date 2022/7/29
 */
@Data
public class RegistResult {
    @JSONField(name = "Data")
    private int Data;

    @JSONField(name = "Nonce")
    private String Nonce;

    @JSONField(name = "Cnonce")
    private String Cnonce;

    @JSONField(name = "Resign")
    private String Resign;

    @JSONField(name = "ResponseString")
    private String ResponseString;
}
