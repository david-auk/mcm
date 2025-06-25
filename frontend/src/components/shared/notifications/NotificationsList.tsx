import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import './NotificationsList.css';
import ItemTooltip from './ItemTooltip';

// In‐module caches to dedupe fetches
const dataCache: Record<string, RawNotification[]> = {};
const promiseCache: Record<string, Promise<RawNotification[]>> = {};

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

interface NotificationsListProps {
  endpoint: string;
}

const NotificationsList: React.FC<NotificationsListProps> = ({ endpoint }) => {
  const [notifications, setNotifications] = useState<RawNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const toast = useToast();

  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    // If we already fetched & cached data, use it immediately
    if (dataCache[endpoint]) {
      setNotifications(dataCache[endpoint]);
      setLoading(false);
      return;
    }

    // If a fetch promise is already in flight, reuse it; otherwise make one
    if (!promiseCache[endpoint]) {
      promiseCache[endpoint] = authenticatedFetch
        .get<RawNotification[]>(endpoint)
        .then(res => res.data);
    }

    promiseCache[endpoint]!
      .then(data => {
        dataCache[endpoint] = data;
        if (!cancelled) {
          setNotifications(data);
        }
      })
      .catch(err => {
        console.error(err);
        if (!cancelled) {
          toast(
            err.response?.data?.error ||
            `Failed to load notifications from ${endpoint}`,
            'error'
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [endpoint, toast]);

  // reuse your template‐parsing logic
  const renderMessageNodes = (n: RawNotification) => {
    const tmpl = n.message_template.messageTemplate;
    const parts = tmpl.split(/\$\{([^}]+)\}/g);

    return parts.map((part, idx) => {
      if (idx % 2 === 1) {
        const val = n.vars[part];
        if (val && typeof val === 'object') {
          return (
            <ItemTooltip key={idx} data={val as Record<string, any>}>
              <span className="notification__token">
                {String((val as any).username ?? (val as any).name ?? part)}
              </span>
            </ItemTooltip>
          );
        }
        return (
          <code key={idx} className="notification__primitive">
            {String(val)}
          </code>
        )
      }
      return <span key={idx}>{part}</span>;
    });
  };

  if (loading) return <p>Loading notifications…</p>;
  if (notifications.length === 0) return <p>No notifications.</p>;

  return (
    <div className="notifications-list">
      {notifications.map((n, i) => {
        const sev = n.message_template.severity ?? 'info';
        return (
          <div key={i} className={`notification notification--${sev}`}>
            <span className="notification__icon">
              {sev === 'info' && 'ℹ'}
              {sev === 'success' && '✔'}
              {sev === 'error' && '✖'}
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

export default NotificationsList;
