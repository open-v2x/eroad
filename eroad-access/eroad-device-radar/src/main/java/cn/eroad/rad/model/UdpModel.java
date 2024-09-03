package cn.eroad.rad.model;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public class UdpModel {
    private ChannelHandlerContext ctx;
    private DatagramPacket packet;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }

    public UdpModel(ChannelHandlerContext ctx, DatagramPacket packet) {
        this.ctx = ctx;
        this.packet = packet;
    }
}
