import axios from "axios";
import { signAndTime, urlCheckout } from '@/assets/js/api.js'
export const trafficLight = (data) => axios({
  url: urlCheckout(
    '/service-api/exposureAPIS/path/taffic_light/timing_plan',
    '/service-api/exposureAPIS/path/taffic_light/timing_plan'
  ),
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
});

export const road_intersection = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_cross_relation_list',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_tongxing_tian = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tongxing_tian',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_tongxing_duan = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tongxing_duan',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_yongdu_tian = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_yongdu_tian',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_yongdu_duan = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_yongdu_duan',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const event_day = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/event_day',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const event_hour = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/event_hour',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const cross_prop = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/cross_prop',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const cross_prop_m_n = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/cross_prop_m_n',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_wait_num = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_wait_num',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_section_cross_flow_tian = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_section_cross_flow_tian',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_section_cross_flow_duan = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_section_cross_flow_duan',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

export const road_cross_relation_list = (data) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_cross_relation_list',
  method: 'post',
  headers: Object.assign({
    'Authorization': null,
    'Content-Type': 'application/json'
  }, signAndTime(data)),
  data
})

