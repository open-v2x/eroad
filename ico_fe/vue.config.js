const path = require('path');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
require('events').EventEmitter.defaultMaxListeners = 20;
const resolve = (dir) => path.join(__dirname, dir);
module.exports = {
  publicPath: './',
  outputDir: 'dist',
  lintOnSave: true,
  runtimeCompiler: false,
  transpileDependencies: [],
  //productionSourceMap: false,
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', resolve('src'));
  },
  configureWebpack: {
    amd: {
      // Enable webpack-friendly use of require in Cesium
      toUrlUndefined: true,
    },
    plugins: [
      // Copy Cesium Assets, Widgets, and Workers to a static directory
      new CopyWebpackPlugin({
        patterns: [
          { from: path.join('node_modules/cesium/Source', '../Build/Cesium/Workers'), to: 'Workers' },
          { from: path.join('node_modules/cesium/Source', 'Assets'), to: 'Assets' },
          { from: path.join('node_modules/cesium/Source', 'Widgets'), to: 'Widgets' },
          { from: path.join('node_modules/cesium/Source', 'ThirdParty'), to: 'ThirdParty' },
          { from: 'node_modules/@liveqing/liveplayer-v3/dist/component/crossdomain.xml' },
          { from: 'node_modules/@liveqing/liveplayer-v3/dist/component/liveplayer.swf' },
          { from: 'node_modules/@liveqing/liveplayer-v3/dist/component/liveplayer-lib.min.js', to: 'static/js/' },
        ],
      }),
      new webpack.DefinePlugin({
        // Define relative base path in cesium for loading assets
        CESIUM_BASE_URL: JSON.stringify(''),
      }),
    ],
    module: {
      unknownContextCritical: false,
      rules: [
        {
          test: /\.js$/,
          include: path.resolve(__dirname, 'node_modules/cesium/Source'),
          use: [
            '@open-wc/webpack-import-meta-loader',
          ],
        },
        {
          test: /\.mjs$/,
          include: /node_modules/,
          type: 'javascript/auto',
        },
        {
          test: /\.geojson$/,
          loader: 'json-loader'
        }
      ],
    },
  },
  devServer: {
    host: '0.0.0.0',
    port: '16666',
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
    proxy: {
      '/service-api': {
        target: 'http://10.1.4.48:1600',
        changeOrigin: true,
      },
      '/device': {
        target: 'http://127.0.0.1:14080',
        changeOrigin: true,
      },
      '/system': {
        target: 'http://127.0.0.1:14080',
        changeOrigin: true,
      },
      '/scenarios':{
        target: 'https://city189.cn:3101'
      },
      '/api/auth': {
        target: 'https://city189.cn:2604',
        changeOrigin: true,
      },
      '/v3': {
        target: 'https://restapi.amap.com',
        changeOrigin: true,
      },
      '/tiles-server': {//http://192.168.200.111/
        // target: 'http://192.168.200.111:1602',
        target: 'http://city189.cn:1602',
        changeOrigin: true,
      },
      '/video': {
        target: 'http://city189.cn:1602',
        changeOrigin: true,
      },
      '/api': {
        // 测试1554 生产1501
        target: 'https://city189.cn:1501',
        changeOrigin: true,
      }
    },
  },
};
