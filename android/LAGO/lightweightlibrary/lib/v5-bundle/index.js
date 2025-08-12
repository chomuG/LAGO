// v5.0.8 UMD 번들 진입점
import { createChart } from 'lightweight-charts';

// UMD 방식으로 전역 객체에 노출
window.LightweightChartsV5 = {
    createChart,
    version: () => '5.0.8'
};

console.log('✅ LightweightCharts v5.0.8 UMD bundle loaded successfully!');
console.log('Available as window.LightweightChartsV5');