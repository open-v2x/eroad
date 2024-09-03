package cn.eroad.videocast.model.config;

import lombok.Data;

@Data
public class UpServerConfigure {
    private Integer Enabled;
    private Integer AddressType;
    private String Address;
    private Integer Port;
    private String DeviceCode;
    private String PIN;
    private String RSAPublicKey;
}
