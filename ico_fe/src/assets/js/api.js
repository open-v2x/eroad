import { fetch } from './http';
import axios from 'axios';
import HmacSHA256 from '@/assets/config/hmac_sha256.js'
import HashMap from '@/assets/config/hashMap.js'
export const signAndTime = (data) => { // 请求头签名鉴权
  let time = new Date().getTime()
  const hashMap = new HashMap()
  hashMap.put('param', data)
  hashMap.put('timestamp', time)
  const str = hashMap.toString()
  let key = '9900J7T2J91992'
  const hmacSHA = new HmacSHA256()
  let sign = hmacSHA.sign(str, key)
  return { sign, timestamp: time }
}
export const urlCheckout = (pro, dev) => { // 由于测试、生产路径不一致，加入切换函数。
  return api_environment === 'development' ? dev : pro
}

export const loginOs = params => axios({
  //url: '/oauth/token',
  url: `/auth/oauth/token?grant_type=${params.grant_type}&username=${params.username}&password=${params.password}=&scope=${params.scope}`,
  method: 'post',
  headers: {
    'Authorization': 'Basic aW9jOmlvYw==',
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  data: params
});
export const loginUserCenter = params => axios({ // 用户中心自动登录
  url: '/auth/oauth/token',
  method: 'post',
  params
})
export const loginToUserCenter = params => axios({
  url: '/login',
  method: 'get',
  params
})

export const getUserInfo = params => axios({
  url: '/auth/resource/getUserInfo',
  method: 'post',
  headers: {
    'Authorization': `Bearer ${JSON.parse(localStorage.getItem('tokenObj')).access_token}`,
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  data: params
});

export const getInfo = params => fetch('/api/get/all', { ...params }, 'POST', false, { withMask: false });
export const getWeather = params => axios({
  url: '/v3/weather/weatherInfo',
  method: 'get',
  params: params
})

/**数据中台 */
const dataToken = null;//localStorage.getItem('dataToken');

export const getDataAuth = (params) => axios({
  url: '/api/auth/login',
  method: 'post',
  headers: {
    'Content-Type': 'application/json',
  },
  data: params
});


export const rad_tra_flow_10min = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/rad_tra_flow_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const real_flow = () => axios({
  url: urlCheckout(
    '/service-api/exposureAPIS/path/jtts/actual_flow',
    '/service-api/exposureAPIS/path/jtts/actual_flow'
  ),
  method: 'post',
  headers: Object.assign({
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  }, signAndTime({})),
  data: {}
})


export const predict_flow = () => {
  return axios({
    url: urlCheckout(
      '/service-api/exposureAPIS/path/jtts/pre_flow',
      '/service-api/exposureAPIS/path/jtts/pre_flow'
    ),
    method: 'post',
    headers: Object.assign({
      'Authorization': dataToken,
      'Content-Type': 'application/json'
    }, signAndTime({})),
    data: {}
  })
}




export const overflow_rate = () => axios({
  url: urlCheckout(
    '/service-api/exposureAPIS/path/jtts/overflow_rate',
    '/service-api/exposureAPIS/path/jtts/overflow_rate'
  ),
  method: 'post',
  headers: Object.assign({
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  }, signAndTime({})),
  data: {}
});



export const crossroad_detail_list = params => axios({
  url: '/service-api/exposureAPIS/path/state_data/crossroad_detail_list',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const road_section_flow_10min = params => axios({
  url: '/service-api/exposureAPIS/path/rad/road_section_flow_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const rad_road_flow_latest = params => axios({
  url: '/service-api/exposureAPIS/path/rad/rad_road_flow_latest',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const ads_sc_rad_tci_10min = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_tci_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const ads_sc_rad_road_accum_tra_flow_10min = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_road_accum_tra_flow_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const ads_sc_rad_road_accum_tra_flow = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_road_accum_tra_flow',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const ads_sc_rad_road_tci_10min = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_road_tci_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const ads_sc_rad_tra_way_flow_10min = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_tra_way_flow_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});

export const road_section_avgspeed_10min = params => axios({
  url: '/service-api/exposureAPIS/path/sc/road_section_avgspeed_10min',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const exportWord = (params) => axios({
  url: '/export/ioc_report',
  method: 'get',
  responseType: 'blob',
  params
})


export const waitNum = () => axios({
  url: '/service-api/exposureAPIS/path/rad/wait_num_cnt_16h',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: { crossroad_name: 'total' }
})



export const busLine = () => axios({
  url: '/service-api/exposureAPIS/path/bus/dim_bus_line_site/D0893EE7D36598184C99FA91353EF63EC763AB1D43446DD3',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
})


export const tripRatio = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/bus_passenger_ratio_d/877560E0BC5A941F85BC46241789505C0F33F7A0AD37069E30C40D2A35795C96',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
})


export const trafficFlow = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/bus_passenger_10min/877560E0BC5A941F85BC46241789505C4A0FB8832E3182A9',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
})






export const getTrafficJam = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/rad_road_tci_10min/EA1D05C8612BEFFFEC4D45AD57F490A7C426B1038CB303BB',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});

export const getCarDetail = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_accum_tra_way_flow',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});



export const getCarCatagoryRatio = () => axios({
  url: '/service-api/exposureAPIS/path/rad/road_accum_tra_way_ratio',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
});

















export const getRoadTci = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_10min3',
  method: 'post',
  data: params
})



export const getRoadTciList = () => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_10min2',
  method: 'post',
  data: {}
})



export const getRoadTciList2 = () => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_10min22',
  method: 'post',
  data: {}
})



export const getTrafficFlow = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tra_flow_10min_opt_three',
  method: 'post',
  data: params
})












export const getCrossName = (params, cancelToken) => axios({
  cancelToken: cancelToken ? cancelToken : null,
  url: '/service-api/exposureAPIS/path/rad/devicetocrossroad',
  method: 'post',
  data: params
})



export const getAllTciFlow = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_noname3',
  method: 'post',
  data: params
})



export const getAllTciFlow2 = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_withname3',
  method: 'post',
  data: params
})



export const getAllTrafficFlow = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_accum_tra_flow_10minv2_2',
  method: 'post',
  data: params
})



export const getAllTrafficFlow2 = (params) => axios({
  url: '/service-api/exposureAPIS/path/road/tra_flow_10min_opt1/1D77BB3B6AA1ADA77E583ED26E5748714F88EE2982011C664DB02F93383C0102',
  method: 'post',
  data: params
})



export const getIllegalStop = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/illegal_stop_30min/FC3912689E10ED031E76213B23FBA126FF7D1C723AA66607',
  method: 'post',
  data: {}
})



export const getTotalTrafficFlow = (params) => axios({
  url: '/service-api/exposureAPIS/path/sdk/total_traffic_flow/53B6FE7D9606031AB33D9137C25E8A421D4D931D17FA6094',
  method: 'post',
  data: params
})


export const getSdkCarRatio = params => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_ai_sdk_car_type_10min/F5EDB5AF21E663BDF851FDE862A2FF67DA7AB87D08550A50E5ADFF184FEAAF4B4DB02F93383C0102',
  method: 'post',
  data: {}
})


export const getAvgSpeed = params => axios({
  url: '/service-api/exposureAPIS/path/rad/avg_speed_10min_total_avg/24439C731D4651A05DF5D1A0931864DA555018E2EB5128CEBBD92619E99A34C9',
  method: 'post',
  data: {}
})


export const getOfflineEventData = params => axios({
  url: '/service-api/exposureAPIS/path/event/statistics_today_count/0C204FFBF3AE9389C8C878F56EFDF953572CC32C3B408A8B8B1541316D1778BD',
  method: 'post',
  data: params
})

export const getSpeedDetail = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_avg_speed_realtime/F5EDB5AF21E663BDBD28D1E26123481CD8BB83A79489D82E4E07356E0FBDF2F230C292DA930FC45A',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const getTotalSpeed = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/avg_speed_10min_total_avg/24439C731D4651A05DF5D1A0931864DA555018E2EB5128CEBBD92619E99A34C9',
  method: 'post',
  data: params
})




export const getInletFlow = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_sc_rad_tra_flow_10min/F5EDB5AF21E663BDD4832E8C25117B6E306D8A987E4985A6D295CEDDC8839DC7',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});

export const getDevice = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/ads_chaowei_device_managen/F5EDB5AF21E663BDE59E624EF550810FF958938942DAD3FC8A6B730FD2622230',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});

export const road_section_jam = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_section_jam',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const total_avgspeed = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/total_avgspeed',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const trafficFlowPerhour = (params) => axios({
  url: '/service-api/exposureAPIS/path/sdk/cross_traffic_flow_perhour',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const roadDetailJam = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tci_10min3',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const roadDetailSpeed = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/avg_speed_10min1',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const roadDetailPerMonth = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tra_flow_10min_opt',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const eventDetail = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/event_statistics_realtime/78EA3DDCD75D1CA1CC64926939D37E7414F68EF9E2ACCBBCAD415063E3028B18',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});


export const flowRatio = (params) => axios({
  url: '/service-api/exposureAPIS/path/rad/road_tra_flow_10min_opt_ratio',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: params
});



export const standard = (data) => axios({
  url: '/service-api/exposureAPIS/path/signal_control_optimizing/standard_index_10min/2347AF73714E9B26B603887AE110D80FD0673DB3A43FB07A8775F3DC817ABBDC85FF7A943CC5A66DC426B1038CB303BB',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data
});


export const line = (data) => axios({
  url: '/service-api/exposureAPIS/path/signal_control_optimizing/index_linechart/2347AF73714E9B26B603887AE110D80FD0673DB3A43FB07A9246D9A2D939C222ABDD92B5B54F20C5C3D65CC180C68427',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data
});



export const newEnergy = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/week_xny_ratio/C1225E952FC6685CA77B81EA9EC40E0AD5D59B72C1FA40E9',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
});


export const bigBrand = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/ads_sc_ai_sdk_car_da_brand_d/4CF3ADE6C7D4722EF851FDE862A2FF67E793D70755C3E3C5E8B51ABBA33C1EA54DB02F93383C0102',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
});

export const smallBrand = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/ads_sc_ai_sdk_car_xiao_brand_d/4CF3ADE6C7D4722EF851FDE862A2FF6716A5551BFFFAE95BE84375F9F8716255D3B6DFA40C881B36',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
});

export const price = () => axios({
  url: '/service-api/exposureAPIS/path/sdk/ads_sc_ai_sdk_car_price_d/4CF3ADE6C7D4722EF851FDE862A2FF67BAC375C1086A8D53E212564486D6F77D',
  method: 'post',
  headers: {
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  },
  data: {}
});


export const road_intersection = params => axios({
  url: '/service-api/exposureAPIS/path/rad/road_cross_relation_list',
  method: 'post',
  headers: {
    'Content-Type': 'application/json'
  },
  data: {}
})


export const trafficLight = (data) => axios({
  url: '/service-api/exposureAPIS/path/taffic_light/timing_plan/E803AAF83AB564D0C371C5F1388EA691D5593CD4ADF62B714DB02F93383C0102?applicationName=主题分析-红绿灯配时&appKey=2208AD707138FD337B9CF885A55E1B441BAD1A08EFB6EA18D5F0D999B18D58CD',
  headers: Object.assign({
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  }, signAndTime({})),
  method: 'post',
  data
});


export const getEventDay = (data) => axios({
  url: '/event_day',
  headers: Object.assign({
    'Authorization': dataToken,
    'Content-Type': 'application/json'
  }, signAndTime({})),
  method: 'post',
  data
});
