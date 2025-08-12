const path = require('path');

const config = {
    mode: "production",
    optimization: {
        minimize: false
    },
    entry: './lib/v5-bundle/index.js',
    output: {
        filename: 'lightweight-charts-v5.bundle.js',
        library: 'LightweightCharts',
        libraryTarget: 'umd',
        globalObject: 'this'
    },
    resolve: {
        alias: {
            lightweight$: path.resolve(__dirname, './node_modules/lightweight-charts/dist/lightweight-charts.standalone.production.js'),
        },
    },
};

module.exports = () => {
    return config;
};