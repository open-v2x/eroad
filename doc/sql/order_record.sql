-- auto-generated definition
create table cmd_record
(
    cmd_id       varchar(32)      not null comment '主键'
        primary key,
    parent_id      varchar(32)      null comment '父命令id',
    operation      varchar(50)      null comment '指令的用途',
    recmd_flag   tinyint          null comment '重发标志',
    rollback_flag  tinyint          null comment '回滚标志',
    http_pre         varchar(32)      null comment '请求前缀含端口',
    req_uri        varchar(200)     null comment '请求URI',
    request_header varchar(200)     null comment '请求头',
    req_method     varchar(20)      null comment '请求方式',
    req_params     text             null comment '请求参数',
    device_ids     text             null comment '被控制的设备的列表',
    resp_status    tinyint unsigned null comment '状态  0：失败   1：成功',
    req_time       int unsigned     null comment '请求时长(毫秒)',
    resp_code      varchar(16)      null comment '返回码',
    resp_data      text             null comment '返回数据',
    creator        varchar(20)      null comment '创建者',
    create_time    datetime         null comment '创建时间'
)
    comment '命令记录表';

