
let wsurl = ''

switch(process.env.NODE_ENV) {
  case 'development':
    wsurl = 'wss://city189.cn:1684'
    break
  case 'production':
    wsurl = 'wss://city189.cn:1684'
    break
}

export const wsUrl = wsurl