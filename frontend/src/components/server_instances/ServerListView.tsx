// src/components/server_instances/ServerListView.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ServerListView.css';
import type ServerInstance from '../../pages/server_instance/ServerInstance';
import { useToast } from '../../contexts/ToastContext';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import ServerInstanceModal from './ServerInstanceModal';
import ImportServerModal from './ImportServerModal';

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
  const [showImport, setShowImport] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();

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
          <div className="actions">
            <button className="secondary" onClick={() => setShowAdd(true)}>
              + Add
            </button>
            <button className="holo" onClick={() => setShowImport(true)}>
              {/* Up-arrow in a box to mirror Export icon semantics later */}
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" aria-hidden>
                <path fill="currentColor" d="M12 3v10.17l3.59-3.58L17 11l-5 5-5-5 1.41-1.41L11 13.17V3h2z" />
                <path fill="currentColor" d="M5 13v6h14v-6h2v8H3v-8h2z" />
              </svg>
              Import
            </button>
          </div>
        )}
      </header>

      {loading ? (
        <p>Loading servers…</p>
      ) : servers.length === 0 ? (
        <p>
          No servers found.
          {allowAdd ? ' Click “Add” to get started.' : ' Ask an admin for access.'}
        </p>
      ) : (
        <ul className="server-list">
          {servers.map(s => (
            <li
              key={s.id}
              className={`server-list__item ${!s.eulaAccepted ? 'server--uninit' : ''
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
        <ServerInstanceModal
          onClose={() => setShowAdd(false)}
          isOpen={false}
          onSaved={() => {
            setShowAdd(false);
            fetchServers();
          }}
          forumMode={'new'} />
      )}

      {showImport && (
        <ImportServerModal
          isOpen={showImport}
          onClose={() => setShowImport(false)}
          onImported={() => {
            setShowImport(false);
            fetchServers();
          }}
        />
      )}
    </div>
  );
};

export default ServerListView;
