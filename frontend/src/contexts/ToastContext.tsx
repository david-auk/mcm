import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from 'react';
import ToastContainer from '../components/ToastContainer';

type ToastType = 'success' | 'error' | 'info';

interface Toast {
  id: number;
  message: string;
  type: ToastType;
  duration: number;
  isExiting: boolean;
}

interface ToastContextProps {
  /**
   * Show a toast.
   * @param message – the text
   * @param type – 'success' | 'error' (defaults to 'success')
   * @param duration – how long (ms) before it starts exiting (defaults to 4000)
   */
  addToast: (
    message: string,
    type?: ToastType,
    duration?: number
  ) => void;
}

const ToastContext = createContext<ToastContextProps | undefined>(undefined);

const DEFAULT_DURATION = 4000;    // 4s stay time
const EXIT_ANIM_DURATION = 300;   // match the CSS exit animation

export const ToastProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const addToast = useCallback(
    (
      message: string,
      type: ToastType = 'success',
      duration: number = DEFAULT_DURATION
    ) => {
      const id = Date.now() + Math.random();
      const newToast: Toast = {
        id,
        message,
        type,
        duration,
        isExiting: false,
      };
      setToasts((prev) => [...prev, newToast]);

      // after `duration`, mark it exiting (which also moves it to the bottom)
      setTimeout(() => {
        setToasts((prev) => {
          const exiting = prev.find((t) => t.id === id);
          const others = prev.filter((t) => t.id !== id);
          if (!exiting) return prev;
          return [
            ...others,
            { ...exiting, isExiting: true },
          ];
        });
      }, duration);

      // after duration + exit animation, remove it
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, duration + EXIT_ANIM_DURATION);
    },
    []
  );

  return (
    <ToastContext.Provider value={{ addToast }}>
      {children}
      <ToastContainer toasts={toasts} />
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextProps['addToast'] => {
  const ctx = useContext(ToastContext);
  if (!ctx)
    throw new Error('useToast must be used within a ToastProvider');
  return ctx.addToast;
};
