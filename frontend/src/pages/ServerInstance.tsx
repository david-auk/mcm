// src/pages/views/servers/ServerInstanceView.tsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './ServerInstance.css';
import { useToast } from '../contexts/ToastContext';
import authenticatedFetch from '../utils/auth/authenticatedFetch';
import { isAdmin } from '../utils/auth/userDetails';
import TabView from '../components/shared/views/TabView';

interface ServerInstance {
  id: string;
  name: string;
  description?: string;
  minecraftVersion: string;
  jarUrl: string;
  eulaAccepted: boolean; // “initialized”
  createdAt: string;
  allocatedRamMB: number;
  port: number;
  running: boolean;
}

const ServerInstanceView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [server, setServer]       = useState<ServerInstance | null>(null);
  const [loading, setLoading]     = useState(true);
  const [initLoading, setInitLoading] = useState(false);
  const toast                      = useToast();
  const navigate                   = useNavigate();

  // fetch server details
  useEffect(() => {
    if (!id) return;
    setLoading(true);
    authenticatedFetch
      .get<ServerInstance>(`/server-instances/${id}`)
      .then(({ data }) => setServer(data))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.message || 'Failed to load server', 'error');
        navigate('/servers');
      })
      .finally(() => setLoading(false));
  }, [id, toast, navigate]);

  // initialization handler
  const handleInitialize = async () => {
    if (!server) return;
    setInitLoading(true);
    try {
      await authenticatedFetch.post(`/server-instances/initialize/${server.id}`);
      toast('Server initialized!', 'success');
      // re-fetch state
      const { data } = await authenticatedFetch.get<ServerInstance>(`/server-instances/${id}`);
      setServer(data);
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.message || 'Initialization failed', 'error');
    } finally {
      setInitLoading(false);
    }
  };

  if (loading) return <p>Loading server…</p>;
  if (!server) return null;

  // build your tabs
  const tabs = [];

  // Overview tab always visible
  tabs.push({
    label: 'Overview',
    path: 'overview',
    component: (
      <div className="details-panel">
        <p><strong>Description:</strong> {server.description || '—'}</p>
        <p><strong>Version:</strong> {server.minecraftVersion}</p>
        <p><strong>JAR URL:</strong> <a href={server.jarUrl} target="_blank" rel="noreferrer">{server.jarUrl}</a></p>
        <p><strong>RAM:</strong> {server.allocatedRamMB} MB</p>
        <p><strong>Port:</strong> {server.port}</p>
        <p><strong>Running:</strong> {server.running ? 'Yes' : 'No'}</p>
      </div>
    ),
  });

  // If not initialized and user is admin, show Initialize tab
  if (server.eulaAccepted) {


  } else {
    if (isAdmin()) {
        tabs.push({
        label: 'Initialize',
        path: 'initialize',
        component: (
            <div className="init-panel">
            <p>This server must be initialized before use.</p>
            <button
                className="btn btn--primary"
                onClick={handleInitialize}
                disabled={initLoading}
            >
                {initLoading ? 'Initializing…' : 'Initialize'}
            </button>
            </div>
        ),
        });
    }
  }

  return (
    <main>
      <TabView
        tabs={tabs}
        title={server.name}
        subtitle={
          server.eulaAccepted
            ? server.description || undefined
            : 'Not initialized'
        }
      />
    </main>
  );
};

export default ServerInstanceView;
