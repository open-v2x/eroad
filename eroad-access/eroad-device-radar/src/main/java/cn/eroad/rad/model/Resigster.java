package cn.eroad.rad.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resigster {
    private String sn;
    private String vender;
    private String ctlVer;
    private Double longitude;
    private Double latitude;
    private Float altitude;
    private String ipv4Gateway;
    private String ipv4Mask;
    private String ipv4Address;
    private String ipv6Gateway;
    private String ipv6Mask;
    private String ipv6LlaAddress;
    private String ipv6GuaAddress;
    private Integer localPort;
    private Integer targetPort;
    private Integer pcUpPort;
    private Integer heartbeatCycle;
    private String mac;
    private String targetIp;
}
