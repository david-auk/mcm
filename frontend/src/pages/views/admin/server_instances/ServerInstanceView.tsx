// src/pages/views/servers/ServerInstancesView.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ServerInstancesView.css';
import { useToast } from '../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import AddServerInstanceModal from './AddServerInstance';

interface ServerInstance {
  id: string;
  name: string;
  description?: string;
  minecraftVersion: string;
  jarUrl: string;
  eulaAccepted: boolean;   // “initialized”
  createdAt: string;
  allocatedRamMB: number;
  port: number;
  running: boolean;
}

const ServerInstanceView: React.FC = () => {
  const [servers, setServers]       = useState<ServerInstance[]>([]);
  const [loading, setLoading]       = useState(true);
  const [showAdd, setShowAdd]       = useState(false);
  const toast                        = useToast();
  const navigate                     = useNavigate();

  const fetchServers = async () => {
    setLoading(true);
    try {
      const { data } = await authenticatedFetch.get<ServerInstance[]>('/server-instances');
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
  }, []);

  return (
    <div className="server-list-container">
      <header className="server-list-header">
        <h2>Your Servers</h2>
        <button className="btn btn--primary" onClick={() => setShowAdd(true)}>
          + Add Server
        </button>
      </header>

      {loading ? (
        <p>Loading servers…</p>
      ) : servers.length === 0 ? (
        <p>No servers yet. Click “Add Server” to get started.</p>
      ) : (
        <ul className="server-list">
          {servers.map(s => (
            <li
              key={s.id}
              className={`server-list__item ${!s.eulaAccepted ? 'server--uninit' : ''}`}
              onClick={() => navigate(`/server-instance/${s.id}`)}
            >
              <span className="server-name">{s.name}</span>
              {!s.eulaAccepted && <span className="badge badge--warning">Initialize</span>}
            </li>
          ))}
        </ul>
      )}

      {showAdd && (
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

export default ServerInstanceView;
