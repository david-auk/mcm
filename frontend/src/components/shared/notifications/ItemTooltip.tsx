// src/components/shared/ItemTooltip.tsx
import React, {
  type ReactNode,
  useState,
  useRef,
  useLayoutEffect,
  useEffect,
} from 'react';
import ReactDOM from 'react-dom';
import './ItemTooltip.css';

interface ItemTooltipProps {
  data: Record<string, any>;
  children: ReactNode;
}

const PADDING = 8;

const ItemTooltip: React.FC<ItemTooltipProps> = ({ data, children }) => {
  const [visible, setVisible] = useState(false);
  const [pos,    setPos]    = useState({ top: 0, left: 0 });
  const wrapperRef = useRef<HTMLSpanElement>(null);
  const popupRef   = useRef<HTMLDivElement>(null);

  const updatePosition = () => {
    if (!wrapperRef.current || !popupRef.current) return;
    const t = wrapperRef.current.getBoundingClientRect();
    const p = popupRef.current.getBoundingClientRect();
    const vw = window.innerWidth, vh = window.innerHeight;

    let top  = t.bottom + PADDING;
    let left = t.left;
    if (left + p.width + PADDING > vw)    left = vw - p.width - PADDING;
    if (left < PADDING)                   left = PADDING;
    if (top + p.height + PADDING > vh)    top = t.top - p.height - PADDING;
    if (top < PADDING)                    top = PADDING;

    setPos({ top, left });
  };

  // initial calc before paint
  useLayoutEffect(() => {
    if (visible) updatePosition();
  }, [visible, data]);

  // while visible, constantly update on each frame
  useEffect(() => {
    if (!visible) return;
    let rafId: number;
    const loop = () => {
      updatePosition();
      rafId = requestAnimationFrame(loop);
    };
    loop();
    return () => cancelAnimationFrame(rafId);
  }, [visible, data]);

  return (
    <span
      ref={wrapperRef}
      className="item-tooltip__wrapper"
      onMouseEnter={() => setVisible(true)}
      onMouseLeave={() => setVisible(false)}
    >
      {children}
      {visible &&
        ReactDOM.createPortal(
          <div
            ref={popupRef}
            className="item-tooltip__popup"
            style={{ top: pos.top, left: pos.left }}
          >
            {Object.entries(data).map(([key, val]) => (
              <div key={key} className="item-tooltip__stat">
                <span className="item-tooltip__stat-key">{key}:</span>{' '}
                <span className="item-tooltip__stat-val">{String(val)}</span>
              </div>
            ))}
          </div>,
          document.body
        )}
    </span>
  );
};

export default ItemTooltip;