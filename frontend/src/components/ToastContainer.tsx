// src/components/ToastContainer.tsx
import React from 'react';
import './Toast.css';

interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
  isExiting: boolean;
}

const ToastContainer: React.FC<{ toasts: Toast[] }> = ({ toasts }) => (
  <div className="toast-wrapper">
    {toasts.map((t) => (
      <div
        key={t.id}
        className={`toast toast--${t.type} ${t.isExiting ? 'toast--exit' : ''}`}
      >
        <span className="toast__icon" />
        <div className="toast__message">{t.message}</div>
      </div>
    ))}
  </div>
);

export default ToastContainer;
