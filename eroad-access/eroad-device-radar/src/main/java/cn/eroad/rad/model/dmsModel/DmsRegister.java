package cn.eroad.rad.model.dmsModel;

import cn.eroad.rad.model.Network;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DmsRegister {

    private String id;
    private String sn;
    private String vender;
    private String ctlVer;
    private Double longitude;
    private Double latitude;
    private Float altitude;
    private Network net;
    private List<Integer> deviceStat;
}
