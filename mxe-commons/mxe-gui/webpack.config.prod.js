const path = require('path');
const FileManagerPlugin = require('filemanager-webpack-plugin');
const LicensePlugin = require('webpack-license-plugin');
const WebpackShellPlugin = require('webpack-shell-plugin');

const targetPackage = path.resolve(__dirname, 'target/public');
const externals = require('./externals.config');
const utils = require('./webpack.utils');

module.exports = {
  entry: utils.getEntries('prod'),
  output: {
    filename: '[name].js',
    libraryTarget: 'amd',
    path: targetPackage,
  },
  externals,
  mode: 'production',
  resolve: {
    alias: utils.getAliases(),
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
  plugins: [
    new LicensePlugin({ outputFilename: '3pp-licenses.json', perChunkOutput: false }),
    new FileManagerPlugin(utils.getFileManagerPluginConfig(targetPackage)),
    // new WebpackShellPlugin(utils.getShellScripts()),
  ],
};
