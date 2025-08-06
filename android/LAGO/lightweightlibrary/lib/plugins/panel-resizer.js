// Panel Resizer Plugin for Lightweight Charts v4
export class PanelResizer {
    constructor(chart) {
        this._chart = chart;
        this._separators = [];
        this._resizeInfo = null;
        this._container = chart._chartWidget._container;
        
        // 패널 크기 상태 (0-1 범위의 비율)
        this._panelSizes = {
            main: 0.6,
            volume: 0.2,
            rsi: 0.1,
            macd: 0.1
        };
        
        this._initSeparators();
    }
    
    _initSeparators() {
        // 각 패널 사이에 separator 생성
        this._createSeparator('main-volume', 0);
        this._createSeparator('volume-rsi', 1);
        this._createSeparator('rsi-macd', 2);
    }
    
    _createSeparator(id, index) {
        const separator = document.createElement('div');
        separator.className = 'panel-separator';
        separator.style.cssText = `
            position: absolute;
            left: 0;
            right: 0;
            height: 3px;
            background: #e0e0e0;
            cursor: row-resize;
            z-index: 100;
            transition: background 0.2s;
        `;
        
        // 호버 효과
        separator.addEventListener('mouseenter', () => {
            separator.style.background = '#2196F3';
            separator.style.height = '5px';
        });
        
        separator.addEventListener('mouseleave', () => {
            if (!this._resizeInfo) {
                separator.style.background = '#e0e0e0';
                separator.style.height = '3px';
            }
        });
        
        // 드래그 이벤트
        separator.addEventListener('mousedown', (e) => this._startResize(e, index));
        
        this._container.appendChild(separator);
        this._separators.push({ element: separator, index });
    }
    
    _startResize(event, separatorIndex) {
        event.preventDefault();
        
        this._resizeInfo = {
            startY: event.pageY,
            separatorIndex,
            startSizes: { ...this._panelSizes }
        };
        
        // 전체 화면 오버레이 (드래그 중 다른 요소 방해 방지)
        const overlay = document.createElement('div');
        overlay.className = 'resize-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            cursor: row-resize;
            z-index: 999;
        `;
        
        document.body.appendChild(overlay);
        this._overlay = overlay;
        
        // 이벤트 리스너
        document.addEventListener('mousemove', this._onMouseMove);
        document.addEventListener('mouseup', this._onMouseUp);
    }
    
    _onMouseMove = (event) => {
        if (!this._resizeInfo) return;
        
        const deltaY = event.pageY - this._resizeInfo.startY;
        const containerHeight = this._container.offsetHeight;
        const deltaRatio = deltaY / containerHeight;
        
        // 어떤 separator인지에 따라 패널 크기 조정
        const { separatorIndex, startSizes } = this._resizeInfo;
        const minSize = 0.05; // 최소 5%
        const maxSize = 0.8;  // 최대 80%
        
        switch (separatorIndex) {
            case 0: // main-volume separator
                this._panelSizes.main = this._clamp(
                    startSizes.main + deltaRatio,
                    minSize,
                    maxSize
                );
                this._panelSizes.volume = this._clamp(
                    startSizes.volume - deltaRatio,
                    minSize,
                    maxSize
                );
                break;
                
            case 1: // volume-rsi separator
                this._panelSizes.volume = this._clamp(
                    startSizes.volume + deltaRatio,
                    minSize,
                    maxSize
                );
                this._panelSizes.rsi = this._clamp(
                    startSizes.rsi - deltaRatio,
                    minSize,
                    maxSize
                );
                break;
                
            case 2: // rsi-macd separator
                this._panelSizes.rsi = this._clamp(
                    startSizes.rsi + deltaRatio,
                    minSize,
                    maxSize
                );
                this._panelSizes.macd = this._clamp(
                    startSizes.macd - deltaRatio,
                    minSize,
                    maxSize
                );
                break;
        }
        
        // 차트 업데이트 트리거
        this._updateChartPanels();
    }
    
    _onMouseUp = () => {
        if (this._overlay) {
            document.body.removeChild(this._overlay);
            this._overlay = null;
        }
        
        // separator 스타일 복원
        if (this._resizeInfo) {
            const separator = this._separators[this._resizeInfo.separatorIndex];
            separator.element.style.background = '#e0e0e0';
            separator.element.style.height = '3px';
        }
        
        this._resizeInfo = null;
        
        document.removeEventListener('mousemove', this._onMouseMove);
        document.removeEventListener('mouseup', this._onMouseUp);
    }
    
    _clamp(value, min, max) {
        return Math.max(min, Math.min(max, value));
    }
    
    _updateChartPanels() {
        // 네이티브 코드로 패널 크기 업데이트 메시지 전송
        if (window.updatePanelSizes) {
            window.updatePanelSizes(this._panelSizes);
        }
    }
    
    updateSeparatorPositions() {
        const containerHeight = this._container.offsetHeight;
        
        // 각 separator의 위치 계산
        const mainEnd = this._panelSizes.main * containerHeight;
        const volumeEnd = mainEnd + this._panelSizes.volume * containerHeight;
        const rsiEnd = volumeEnd + this._panelSizes.rsi * containerHeight;
        
        // separator 위치 업데이트
        if (this._separators[0]) {
            this._separators[0].element.style.top = `${mainEnd - 2}px`;
        }
        if (this._separators[1]) {
            this._separators[1].element.style.top = `${volumeEnd - 2}px`;
        }
        if (this._separators[2]) {
            this._separators[2].element.style.top = `${rsiEnd - 2}px`;
        }
    }
    
    getPanelSizes() {
        return this._panelSizes;
    }
    
    destroy() {
        this._separators.forEach(({ element }) => {
            if (element.parentNode) {
                element.parentNode.removeChild(element);
            }
        });
        this._separators = [];
        
        if (this._overlay) {
            document.body.removeChild(this._overlay);
        }
        
        document.removeEventListener('mousemove', this._onMouseMove);
        document.removeEventListener('mouseup', this._onMouseUp);
    }
}

// 전역으로 노출
window.PanelResizer = PanelResizer;