# Machine Learning Execution Environment

## Setup

### Install the dependencies

Add the registry containing the SDK packages to your NPM configuration:

```bash
npm config set @eui:registry https://arm.lmera.ericsson.se/artifactory/api/npm/proj-e-uisdk-npm-local/
npm config set @eds:registry https://arm.sero.gic.ericsson.se/artifactory/api/npm/proj-eds-npm-local/
```

The npm install command may fail due to the use of corporate
HTTP proxy settings. To work around this issue,
the following settings should be added to your NPM configuration:

```bash
npm config set proxy=null
npm config set https-proxy=null
```

If there are problems connecting please use this alternative proxy setting:

```bash
npm config set proxy=http://www-proxy.lmera.ericsson.se:8080
npm config set https-proxy=http://www-proxy.lmera.ericsson.se:8080
```

Install the EUI-SDK CLI using the following command:

```bash
npm i -g @eui/cli
```

Install dependencies

```bash
npm install
```

### Build the project

```bash
npm run build
```

### Start the project locally (with source maps)

```bash
npm start
```

### Linting the project

```bash
npm run lint
npm lint:prettier
npm int:style
```

### Fix linting the project

```bash
npm run lint-fix
npm lint:prettier-fix
npm int:style-fix
```

### Pushing to the remte

Git push is achieved by running this command.

```bash
git push origin HEAD:refs/for/master
```

This project uses eslint (extending Airbnb with some [custom rules](.eslintrc.js)).
