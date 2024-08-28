const path = require('path');
const externals = require('./externals.config');
const utils = require('./webpack.utils');

const clientRoot = 'client';

module.exports = {
  mode: 'development',
  entry: utils.getEntries('dev'),
  output: {
    path: path.resolve(__dirname, clientRoot),
    filename: '[name].js',
    libraryTarget: 'amd',
  },
  externals,
  resolve: {
    alias: utils.getAliases(),
  },
  devtool: 'inline-source-map',
  devServer: {
    contentBase: [path.resolve(__dirname, 'client')],
    compress: true,
    before: require('./dev/serve-static-files'),
    proxy: [
      {
        context: ['/v1', '/oauth/token', '/v2', '/model-lcm'],
        target: 'http://localhost:3000',
      },
    ],
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        include: [
          path.resolve(__dirname, 'client/components'),
          path.resolve(__dirname, 'client/panels'),
          path.resolve(__dirname, 'client/plugins'),
          path.resolve(__dirname, 'client/apps'),
          path.resolve(__dirname, 'client/utils'),
          path.resolve(__dirname, 'client/services'),
        ],
        loader: 'babel-loader',
        options: {
          plugins: [
            ['@babel/plugin-proposal-decorators', { legacy: true }],
            ['@babel/plugin-proposal-class-properties', { loose: true }],
          ],
        },
      },
      {
        test: /\.(html)/,
        use: {
          loader: 'raw-loader',
          options: {
            exportAsEs6Default: true,
          },
        },
      },
      {
        test: /\.css$/,
        use: ['css-loader', 'postcss-loader'],
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/i,
        use: [
          {
            loader: 'file-loader',
          },
        ],
      },
    ],
  },
};
