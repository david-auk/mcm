// src/pages/views/notifications/NotificationsView.tsx
import React, { useState, useEffect } from 'react';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import './NotificationsView.css';
import ItemTooltip from './ItemToolTip';

interface MessageTemplate {
  name: string;
  messageTemplate: string;
  severity: 'info' | 'success' | 'error';
}

interface RawNotification {
  timestamp: string;
  message_template: MessageTemplate;
  vars: Record<string, unknown>;
}

const NotificationsView: React.FC = () => {
  const [notifications, setNotifications] = useState<RawNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const toast = useToast();

  useEffect(() => {
    authenticatedFetch
      .get<RawNotification[]>('/user/me/notifications')
      .then(({ data }) => setNotifications(data))
      .catch(err =>
        toast(
          err.response?.data?.message || 'Failed to load notifications',
          'error'
        )
      )
      .finally(() => setLoading(false));
  }, [toast]);

  // Splits the template into text and var‐tokens, rendering
  // primitives directly and objects via ItemTooltip.
  const renderMessageNodes = (n: RawNotification) => {
    const tmpl = n.message_template.messageTemplate;
    // split into ["", "user", " changed ...", "old_username", ...]
    const parts = tmpl.split(/\$\{([^}]+)\}/g);

    return parts.map((part, idx) => {
      // odd idx = variable key
      if (idx % 2 === 1) {
        const val = n.vars[part];
        if (val && typeof val === 'object') {
          return (
            <ItemTooltip key={idx} data={val as Record<string, any>}>
              <span className="notification__token">
                {/* display username or name or the key */}
                {String((val as any).username ?? (val as any).name ?? part)}
              </span>
            </ItemTooltip>
          );
        }
        return <span key={idx}>{String(val)}</span>;
      }
      // even idx = literal text
      return <span key={idx}>{part}</span>;
    });
  };

  if (loading) return <p>Loading notifications…</p>;
  if (notifications.length === 0) return <p>No notifications.</p>;

  return (
    <div className="notifications-list">
      {notifications.map((n, i) => {
        const severity = n.message_template.severity ?? 'info';
        return (
          <div
            key={i}
            className={`notification notification--${severity}`}
          >
            <span className="notification__icon">
              {severity === 'info' && 'ℹ'}
              {severity === 'success' && '✔'}
              {severity === 'error' && '✖'}
            </span>
            <div className="notification__body">
              <div className="notification__message">
                {renderMessageNodes(n)}
              </div>
              <small className="notification__timestamp">
                {new Date(n.timestamp).toLocaleString()}
              </small>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default NotificationsView;
