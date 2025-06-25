import React, {
  type ReactNode,
  useState,
  useRef,
  useLayoutEffect,
} from 'react';
import ReactDOM from 'react-dom';
import './ItemTooltip.css';

interface ItemTooltipProps {
  data: Record<string, any>;
  children: ReactNode;
}

const PADDING = 8; // space between trigger and tooltip

const ItemTooltip: React.FC<ItemTooltipProps> = ({ data, children }) => {
  const [visible, setVisible] = useState(false);
  const [pos, setPos] = useState({ top: 0, left: 0 });
  const wrapperRef = useRef<HTMLSpanElement>(null);
  const popupRef   = useRef<HTMLDivElement>(null);

  useLayoutEffect(() => {
    if (!visible || !wrapperRef.current || !popupRef.current) {
      return;
    }

    const triggerRect = wrapperRef.current.getBoundingClientRect();
    const popupRect   = popupRef.current.getBoundingClientRect();
    const vw = window.innerWidth;
    const vh = window.innerHeight;

    // start below trigger
    let top  = triggerRect.bottom + PADDING;
    let left = triggerRect.left;

    // if right edge would overflow, shift left
    if (left + popupRect.width + PADDING > vw) {
      left = vw - popupRect.width - PADDING;
    }
    // if left < padding, clamp
    if (left < PADDING) {
      left = PADDING;
    }

    // if bottom overflows, flip above
    if (top + popupRect.height + PADDING > vh) {
      top = triggerRect.top - popupRect.height - PADDING;
    }
    // if top < padding, clamp 
    if (top < PADDING) {
      top = PADDING;
    }

    setPos({ top, left });
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
                <span className="item-tooltip__stat-key">
                  {key}:
                </span>{' '}
                <span className="item-tooltip__stat-val">
                  {String(val)}
                </span>
              </div>
            ))}
          </div>,
          document.body
        )}
    </span>
  );
};

export default ItemTooltip;
