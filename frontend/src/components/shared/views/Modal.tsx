import React, { type ReactNode, useEffect } from 'react';
import ReactDOM from 'react-dom';
import './Modal.css';

interface ModalProps {
  title: string;
  children: ReactNode;
  onClose: () => void;
  onConfirm: () => void;
  confirmText?: string;
  cancelText?: string;
}

const Modal: React.FC<ModalProps> = ({
  title,
  children,
  onClose,
  onConfirm,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
}) => {
  // Prevent background scroll when modal is open
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, []);

  // Handle Escape to cancel and Enter to confirm
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      } else if (e.key === 'Enter') {
        onConfirm();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [onClose, onConfirm]);


  // Render modal into #modal-root (add a <div id="modal-root"/> in index.html)
  return ReactDOM.createPortal(
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-container"
        onClick={e => e.stopPropagation()}
      >
        <header className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button
            className="modal-close"
            onClick={onClose}
            aria-label="Close"
          >×</button>
        </header>
        <div className="modal-body">{children}</div>
        <footer className="modal-footer">
          <button
            className="modal-btn modal-btn--cancel"
            onClick={onClose}
          >
            {cancelText}
          </button>
          <button
            className="modal-btn modal-btn--confirm"
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </footer>
      </div>
    </div>,
    document.getElementById('modal-root')!
  );
};

export default Modal;
