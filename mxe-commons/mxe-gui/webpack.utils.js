const path = require('path');
const majorVersion = require('./package.json').version.match(/^\d+/)[0];

const clientRoot = 'client';

/**
 * Get webpack entries (apps, components, etc...)
 * @param env
 */
const getEntries = (env) => {
  const externals =
    env === 'prod' ? require('./externals.config.prod') : require('./externals.config.dev');
  const obj = {};
  Object.keys(externals).forEach((category) => {
    const items = externals[category];
    if (category === 'components') {
      Object.keys(items).forEach((categoryItem) => {
        if (categoryItem === 'default') {
          items[categoryItem].forEach((item) => {
            const { path: itemPath, entry } = item;
            const ip = path.join(category, itemPath, 'Main');
            // console.log(`[${ip}]: ${op}`);
            obj[ip] = path.resolve(__dirname, clientRoot, category, itemPath, 'src', `${entry}.js`);
          });
          return;
        }

        if (categoryItem === 'shareable') {
          items[categoryItem].forEach((item) => {
            const { path: itemPath, entry } = item;
            const ip = path.join(category, itemPath, majorVersion, 'Main');
            // console.log(`[${ip}]: ${op}`);
            obj[ip] = path.resolve(__dirname, clientRoot, category, itemPath, 'src', `${entry}.js`);
          });
        }
      });
    } else {
      items.forEach((item) => {
        const { path: itemPath, entry } = item;
        const ip = path.join(category, itemPath, entry);
        // console.log(`[${ip}]: ${op}`);
        obj[ip] = path.resolve(__dirname, clientRoot, category, itemPath, 'src', `${entry}.js`);
      });
    }
  });

  return obj;
};

/**
 * Get aliases for webpack build
 * @return {Object}
 */
const getAliases = () => ({
  apps: path.resolve(__dirname, './client/apps/'),
  components: path.resolve(__dirname, './client/components/'),
  config: path.resolve(__dirname, 'client/config/'),
  utils: path.resolve(__dirname, './client/utils/'),
  services: path.resolve(__dirname, './client/services/'),
  test: path.resolve(__dirname, './test/'),
  plugins: path.resolve(__dirname, './client/plugins'),
  panels: path.resolve(__dirname, './client/panels'),
  assets: path.resolve(__dirname, './client/assets'),
  store: path.resolve(__dirname, './client/store'),
});

/**
 * Get File manager plugin configuration
 * @param targetPackage
 * @return {{onStart: {copy: {destination: *, source: string}[], delete: [*]}, onEnd: {copy: *[]}}}
 */
const getFileManagerPluginConfig = (targetPackage) => ({
  onStart: {
    delete: [targetPackage],
    copy: [
      /**
       * html files
       */
      {
        source: 'client/*.html',
        destination: targetPackage,
      },
    ],
  },
  onEnd: {
    copy: [
      /**
       * container components
       */
      {
        source: 'node_modules/@eui/container/*(components)/**/!(*.map)',
        destination: `${targetPackage}`,
      },
      {
        source: 'node_modules/@eui/container/libs/@eui/container/**/!(*.map)',
        destination: `${targetPackage}/libs/@eui/container`,
      },
      { source: 'node_modules/@eui/container/libs/**/*.js', destination: `${targetPackage}/libs` },
      /**
       * libs
       */
      {
        source:
          'node_modules/@eui/!(container|theme)/!(node_modules)/**/!(*.map|README.md|package.json)',
        destination: `${targetPackage}/libs/@eui`,
      },
      /**
       * assets
       */
      { source: 'node_modules/@eui/container/assets/', destination: `${targetPackage}/assets` },
      { source: 'node_modules/@eui/theme/0/fonts', destination: `${targetPackage}/assets/fonts` },
      /**
       * apps
       */
      { source: 'client/apps/**/*.json', destination: `${targetPackage}/apps` },

      /**
       * components
       */
      {
        source: 'client/components/settings-icon/config.json',
        destination: `${targetPackage}/components/settings-icon/config.json`,
      },

      /**
       * panels
       */
      { source: 'client/panels/**/!(src)/*.js', destination: `${targetPackage}/panels` },
      { source: 'client/panels/**/*.json', destination: `${targetPackage}/panels` },

      /**
       * plugins
       */
      { source: 'client/plugins/**/*.js', destination: `${targetPackage}/plugins` },
      /**
       * assets
       */
      {
        source: 'client/assets/**/*.svg',
        destination: `${targetPackage}/assets`,
      },
      {
        source: 'client/assets/vendor/*.js',
        destination: `${targetPackage}/assets/vendor`,
      },
      {
        source: 'client/assets/vendor/*.css',
        destination: `${targetPackage}/assets/vendor`,
      },
      {
        source: 'client/assets/css/*.css',
        destination: `${targetPackage}/assets/css`,
      },

      /**
       * polyfills
       */
      { source: 'client/polyfills/**/*.js', destination: `${targetPackage}/libs/polyfills` },

      /**
       * locale
       */
      { source: 'client/locale/**/*.json', destination: `${targetPackage}/locale` },

      /**
       * config
       */
      { source: 'client/config/**/*.{json,js}', destination: `${targetPackage}/config` },

      /**
       * html files
       */
      { source: 'client/*.html', destination: targetPackage },

      /**
       * GAS Config files
       */
      { source: 'client/config.json', destination: targetPackage },
      { source: 'client/config.package.json', destination: targetPackage },
    ],
  },
});

/**
 * Get shell scripts
 * @return {{onBuildEnd: [string]}}
 */
const getShellScripts = () => ({
  onBuildEnd: [
    'html-minifier --input-dir ./ --output-dir ./ --file-ext html --collapse-whitespace --remove-comments --remove-redundant-attributes --remove-script-type-attributes --use-short-doctype --sort-class-name --sort-attributes --remove-empty-attributes --remove-empty-elements --minify-css true --minify-js true --ignore-custom-fragments <\\s*eui-base-v0-.*[^>]*>(.*?)<\\s*/\\s*eui-base-v0-.*>',
  ],
});

module.exports = { getEntries, getAliases, getFileManagerPluginConfig, getShellScripts };
