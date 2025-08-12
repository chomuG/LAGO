const path = require('path');

const config = {
    mode: "production",
    optimization: {
        minimize: false
    },
    resolve: {
        alias: {
            lightweight$: path.resolve(__dirname, './node_modules/lightweight-charts/dist/lightweight-charts.standalone.production.js'),
        },
    },
    entry: './lib/app/index.js',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'src/main/assets/com/tradingview/lightweightcharts/scripts/app'),
        library: 'LightweightCharts',
        libraryTarget: 'umd',
        globalObject: 'this'
    }
};

module.exports = () => {
    return config;
};
