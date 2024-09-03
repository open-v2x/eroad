package cn.eroad.videocast.controller;

import cn.eroad.videocast.model.data.*;
import cn.eroad.videocast.service.FormatService;
import cn.eroad.videocast.service.RegistService;
import cn.eroad.videocast.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 数据上报
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/7/8
 */
@RestController
@RequestMapping("/LAPI/V1.0/System/Event/Notification")
@Slf4j
@Api(value = "数据上报")
public class VideoController {
    @Autowired
    RegistService registService;

    @Autowired
    VideoService videoService;

    @Autowired
    FormatService formatService;

    /**
     * 实时数据上报
     *
     * @return
     */
    @PostMapping("/ObjectRealTimeData")
    @ApiOperation(value = "实时目标数据上报")
    public String objectRealTumeData() {
        return null;
    }

    /**
     * 告警数据上报
     *
     * @param alarmDataVo
     * @return
     */
    @PostMapping("/Alarm")
    @ApiOperation(value = "告警数据上报")
    public Response alarm(AlarmDataVo alarmDataVo) {
        registService.deviceAlarm(alarmDataVo);
        return formatService.formatReturn("/Alarm", "");
    }

    /**
     * 交通区域数据
     *
     * @param roadFlowVo
     * @return
     */
    @PostMapping("/RoadFlow")
    @ApiOperation(value = "交通区域数据")
    public Response roadFlow(RoadFlowVo roadFlowVo) {
        videoService.roadFlow(roadFlowVo);
        return formatService.formatReturn("/RoadFlow", "");
    }

    /**
     * 交通统计信息
     *
     * @param trafficFlowVo
     * @return
     */
    @PostMapping("/TrafficFlow")
    @ApiOperation(value = "交通统计信息")
    public Response trafficFlow(TrafficFlowVo trafficFlowVo) {
        videoService.trafficFlow(trafficFlowVo);
        return formatService.formatReturn("/TrafficFlow", "");
    }

    /**
     * 车辆排队信息
     *
     * @param vehicleQueueLenVo
     * @return
     */
    @PostMapping("/VehicleQueueLen")
    @ApiOperation(value = "车辆排队信息")
    public Response vehicleQueueLen(VehicleQueueLenVo vehicleQueueLenVo) {
        videoService.vehicleQueue(vehicleQueueLenVo);
        return formatService.formatReturn("/VehicleQueueLen", "");
    }

    /**
     * 过车数据
     *
     * @param passDataVo
     * @return
     */
    @PostMapping("/PassData")
    @ApiOperation(value = "过车数据")
    public Response passData(PassDataVo passDataVo) {
        videoService.passData(passDataVo);
        return formatService.formatReturn("/PassData", "");
    }

    /**
     * 溢出事件上报
     *
     * @param overFlowVo
     * @return
     */
    @PostMapping("/OverFlow")
    @ApiOperation(value = "溢出事件上报")
    public Response overFlow(OverFlowVo overFlowVo) {
        videoService.overFlow(overFlowVo);
        return formatService.formatReturn("/OverFlow", "");
    }

    /**
     * 行人事件上报
     *
     * @param pedestrianVo
     * @return
     */
    @PostMapping("/Pedestrian")
    @ApiOperation(value = "行人事件上报")
    public Response pedestrian(PedestrianVo pedestrianVo) {
        videoService.pedestrian(pedestrianVo);
        return formatService.formatReturn("/Pedestrian", "");
    }

    /**
     * 非机动车事件上报
     *
     * @param nonmotorListVo
     * @return
     */
    @PostMapping("/NonmotorList")
    @ApiOperation(value = "非机动车事件上报")
    public Response nonmotorList(NonmotorListVo nonmotorListVo) {
        videoService.nonmotorList(nonmotorListVo);
        return formatService.formatReturn("/NonmotorList", "");
    }

    /**
     * 停车事件上报
     *
     * @param parkingListVo
     * @return
     */
    @PostMapping("/ParkingList")
    @ApiOperation(value = "停车事件上报")
    public Response parkingList(ParkingListVo parkingListVo) {
        videoService.parkingList(parkingListVo);
        return formatService.formatReturn("/ParkingList", "");
    }

    /**
     * 超速事件上报
     *
     * @param overSpeedsListVo
     * @return
     */
    @PostMapping("/OverSpeedsList")
    @ApiOperation(value = "超速事件上报")
    public Response overSpeedsList(OverSpeedsListVo overSpeedsListVo) {
        videoService.overSpeedsList(overSpeedsListVo);
        return formatService.formatReturn("/OverSpeedsList", "");
    }

    /**
     * 低速事件上报
     *
     * @param lowSpeedsListVo
     * @return
     */
    @PostMapping("/LowSpeedsList")
    @ApiOperation(value = "低速事件上报")
    public Response lowSpeedsList(LowSpeedsListVo lowSpeedsListVo) {
        videoService.lowSpeedsList(lowSpeedsListVo);
        return formatService.formatReturn("/LowSpeedsList", "");
    }

    /**
     * 逆行事件上报
     *
     * @param retrogradeListVo
     * @return
     */
    @PostMapping("/RetrogradeList")
    @ApiOperation(value = "逆行事件上报")
    public Response retrogradeList(RetrogradeListVo retrogradeListVo) {
        videoService.retrogradeList(retrogradeListVo);
        return formatService.formatReturn("/RetrogradeList", "");
    }

    /**
     * 拥堵事件上报
     *
     * @param congestionListVo
     * @return
     */
    @PostMapping("/CongestionList")
    @ApiOperation(value = "拥堵事件上报")
    public Response congestionList(CongestionListVo congestionListVo) {
        videoService.congestionList(congestionListVo);
        return formatService.formatReturn("/CongestionList", "");
    }

    /**
     * 变道事件上报
     *
     * @param laneChangeListVo
     * @return
     */
    @PostMapping("/LaneChangeList")
    @ApiOperation(value = "变道事件上报")
    public Response laneChangeList(LaneChangeListVo laneChangeListVo) {
        videoService.laneChangeList(laneChangeListVo);
        return formatService.formatReturn("/LaneChangeList", "");
    }

    /**
     * 占用紧急车道事件上报
     *
     * @param occupancyEmergenctListVo
     * @return
     */
    @PostMapping("/OccupancyEmergenctList")
    @ApiOperation(value = "占用紧急车道事件上报")
    public Response occupancyEmergenctList(OccupancyEmergenctListVo occupancyEmergenctListVo) {
        videoService.occupancyEmergenctList(occupancyEmergenctListVo);
        return formatService.formatReturn("/OccupancyEmergenctList", "");
    }

    /**
     * 区域入侵事件上报
     *
     * @param restrictedAreaListVo
     * @return
     */
    @PostMapping("/RestrictedAreaList")
    @ApiOperation(value = "区域入侵事件上报")
    public Response restrictedAreaLiat(RestrictedAreaListVo restrictedAreaListVo) {
        videoService.restrictedAreaLiat(restrictedAreaListVo);
        return formatService.formatReturn("/RestrictedAreaList", "");
    }
}
