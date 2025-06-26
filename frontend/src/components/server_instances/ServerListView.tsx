// src/pages/views/servers/ServerListView.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ServerListView.css';
import type ServerInstance from '../../pages/server_instance/ServerInstance';
import { useToast } from '../../contexts/ToastContext';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import AddServerInstanceModal from './AddServerInstance';

interface ServerListViewProps {
  /** API endpoint to GET an array of ServerInstance */
  endpoint: string;
  /** If true, shows the “+ Add Server” button & modal */
  allowAdd: boolean;
}

const ServerListView: React.FC<ServerListViewProps> = ({ endpoint, allowAdd }) => {
  const [servers, setServers] = useState<ServerInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const toast                  = useToast();
  const navigate               = useNavigate();

  const fetchServers = async () => {
    setLoading(true);
    try {
      const { data } = await authenticatedFetch.get<ServerInstance[]>(endpoint);
      setServers(data);
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.message || 'Could not load servers', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServers();
  }, [endpoint]);

  return (
    <div className="server-list-container">
      <header className="server-list-header">
        <h2>Your Servers</h2>
        {allowAdd && (
          <button
            className="btn btn--primary"
            onClick={() => setShowAdd(true)}
          >
            + Add Server
          </button>
        )}
      </header>

      {loading ? (
        <p>Loading servers…</p>
      ) : servers.length === 0 ? (
        <p>
          No servers found.
          {allowAdd && ' Click “Add Server” to get started.'}
          {!allowAdd && ' Ask an admin for access.'}
        </p>
      ) : (
        <ul className="server-list">
          {servers.map(s => (
            <li
              key={s.id}
              className={`server-list__item ${
                !s.eulaAccepted ? 'server--uninit' : ''
              }`}
              onClick={() => navigate(`/server-instance/${s.id}`)}
            >
              <span className="server-name">{s.name}</span>
              {!s.eulaAccepted && (
                <span className="badge badge--warning">Initialize</span>
              )}
            </li>
          ))}
        </ul>
      )}

      {allowAdd && showAdd && (
        <AddServerInstanceModal
          onClose={() => setShowAdd(false)}
          onCreated={() => {
            setShowAdd(false);
            fetchServers();
          }}
        />
      )}
    </div>
  );
};

export default ServerListView;
