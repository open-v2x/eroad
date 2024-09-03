package cn.eroad.app.ods.rad.rt;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceCrossInfo implements Serializable {
    public String deviceId;
    public String lampId;
    public Double lampLongitude;
    public Double lampLatitude;
    public String crossId;
    public String crossName;
    public Double crossLongitude;
    public Double crossLatitude;
    public String supplier;

    public DeviceCrossInfo(){}

    public DeviceCrossInfo(ResultSet resultSet) throws SQLException {
        this.deviceId = resultSet.getString("device_id");
        this.lampId = resultSet.getString("lamp_id");
        this.lampLongitude = resultSet.getDouble("lamp_longitude");
        this.lampLatitude = resultSet.getDouble("lamp_latitude");
        this.crossId = resultSet.getString("cross_id");
        this.crossName = resultSet.getString("cross_name");
        this.crossLongitude = resultSet.getDouble("cross_longitude");
        this.crossLatitude = resultSet.getDouble("cross_latitude");
        this.supplier = resultSet.getString("supplier");
    }

    @Override
    public String toString() {
        return "DeviceCrossInfo{" +
                "deviceId='" + deviceId + '\'' +
                ", lampId='" + lampId + '\'' +
                ", lampLongitude='" + lampLongitude + '\'' +
                ", lampLatitude='" + lampLatitude + '\'' +
                ", crossId='" + crossId + '\'' +
                ", crossName='" + crossName + '\'' +
                ", crossLongitude='" + crossLongitude + '\'' +
                ", crossLatitude='" + crossLatitude + '\'' +
                ", supplier='" + supplier + '\'' +
                '}';
    }


}

