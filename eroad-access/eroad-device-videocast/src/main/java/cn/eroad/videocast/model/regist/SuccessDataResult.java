package cn.eroad.videocast.model.regist;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.Accessors;

@lombok.Data
@Accessors(chain = true)
public class SuccessDataResult {
    @JSONField(name = "Cnonce")
    @JsonProperty("Cnonce")
    private String Cnonce;

    @JSONField(name = "Resign")
    @JsonProperty("Resign")
    private String Resign;
}
