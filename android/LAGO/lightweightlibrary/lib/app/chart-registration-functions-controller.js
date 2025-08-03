import SeriesFunctionManager from "./series/series-function-manager.js";
import SubscriptionsFunctionManager from "./subscriptions-function-manager";
import TimeScaleFunctionManager from "./time-scale/time-scale-function-manager";
import { logger } from './logger.js';
import { Locator } from "./service-locator/locator.js";
import PriceScaleFunctionManager from "./price-scale/price-scale-function-manager";

export default class ChartRegistrationFunctionsController {

    constructor(chart, functionManager, pluginManager) {
        this.chart = chart
        this.functionManager = functionManager
        this.pluginManager = pluginManager
        this.cache = new Map()
    }

    registerFunctions() {
        const seriesFunctionManager = Locator.resolve(SeriesFunctionManager.name)
        seriesFunctionManager.register()

        const subscriptions = new SubscriptionsFunctionManager(
            this.chart,
            this.functionManager,
            seriesFunctionManager
        )
        subscriptions.register()

        const timeScale = Locator.resolve(TimeScaleFunctionManager.name)
        timeScale.register()
        const priceScale = Locator.resolve(PriceScaleFunctionManager.name)
        priceScale.register()

        this.functionManager.registerFunction("remove", (params, resolve) => {
            this.cache.clear()
            this.chart.remove()
            // 패널 리사이저 정리
            if (window.panelResizer) {
                window.panelResizer.destroy();
                window.panelResizer = null;
            }
        })

        // 패널 크기 업데이트 함수 등록
        this.functionManager.registerFunction("updatePanelSizes", (params, resolve) => {
            console.log("Panel sizes received from JS:", params);
            
            // Android 네이티브로 패널 크기 전달
            if (window.Android && window.Android.updatePanelSizes) {
                window.Android.updatePanelSizes(JSON.stringify(params));
            }
            
            resolve();
        })

        this.functionManager.registerFunction("chartOptions", (params, resolve) => {
            let options = this.chart.options()

            if (options.localization && options.localization.priceFormatter) {
                const fun = options.localization.priceFormatter
                options.localization.priceFormatter = this.pluginManager.getPlugin(fun)
            }

            if (options.localization && options.localization.timeFormatter) {
                const fun = options.localization.timeFormatter
                options.localization.timeFormatter = this.pluginManager.getPlugin(fun)
            }

            if (options.timeScale && options.timeScale.tickMarkFormatter) {
                const fun = options.timeScale.tickMarkFormatter
                options.timeScale.tickMarkFormatter = this.pluginManager.getPlugin(fun)
            }

            resolve(options)
        })
        this.functionManager.registerFunction("chartApplyOptions", (input, resolve) => {
            console.log("=== chartApplyOptions called ===");
            console.log("Input:", input);
            console.log("Options:", input.params.options);
            
            new Promise((resolve) => {
                if (!input.params.options.localization || !input.params.options.localization.priceFormatter) {
                    resolve()
                    return
                }

                const plugin = input.params.options.localization.priceFormatter
                this.pluginManager.register(plugin, (fun) => {
                    input.params.options.localization.priceFormatter = fun
                    logger.d('plugin priceFormatter registered')
                    resolve()
                })
            }).then(() => new Promise((resolve) => {
                if (!input.params.options.localization || !input.params.options.localization.timeFormatter) {
                    resolve()
                    return
                }

                const plugin = input.params.options.localization.timeFormatter
                this.pluginManager.register(plugin, (fun) => {
                    input.params.options.localization.timeFormatter = fun
                    logger.d('plugin timeFormatter registered')
                    resolve()
                })
            })).then(() => new Promise((resolve) => {
                if (!input.params.options.timeScale || !input.params.options.timeScale.tickMarkFormatter) {
                    resolve()
                    return
                }

                const plugin = input.params.options.timeScale.tickMarkFormatter
                this.pluginManager.register(plugin, (fun) => {
                    input.params.options.timeScale.tickMarkFormatter = fun
                    logger.d('plugin tickMarkFormatter registered')
                    resolve()
                })
            })).then(() => {
                console.log("✅ Applying options to chart:", this.chart);
                this.chart.applyOptions(input.params.options);
                console.log("✅ Options applied successfully");
                logger.d('apply options')
                resolve();
            }).catch((error) => {
                console.error("❌ chartApplyOptions error:", error);
                resolve();
            })
        })

        this.functionManager.registerFunction("takeScreenshot", (input, resolve) => {
            const mimeType = input.params.mimeType
            let chartScreenshot = this.chart.takeScreenshot()
            resolve(chartScreenshot.toDataURL(mimeType, 1.0))
        })

    }

}
