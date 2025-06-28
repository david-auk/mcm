// src/components/server_instances/ServerSettings.tsx
import React, { useState } from 'react';
import './ServerSettings.css';
import type ServerInstance from '../ServerInstance';
import { useToast } from '../../../contexts/ToastContext';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import ServerInstanceModal from '../../../components/server_instances/ServerInstanceModal';

interface Props {
  server: ServerInstance;
  onDeleted: (id: string) => void;
  onUpdated: (updated: ServerInstance) => void;
}

const ServerSettings: React.FC<Props> = ({ server, onDeleted, onUpdated }) => {
  const toast = useToast();
  const [showEdit, setShowEdit] = useState(false);

  const confirmAndDelete = async () => {
    if (!window.confirm(`Delete server "${server.name}"? This cannot be undone.`)) {
      return;
    }
    try {
      await authenticatedFetch.delete(`/server-instances/${server.id}`);
      toast(`Deleted "${server.name}"`, 'success');
      onDeleted(server.id);
    } catch (e: any) {
      toast(e.response?.data?.error || 'Delete failed', 'error');
    }
  };

  return (
    <div className="server-settings">
      <h2>Settings for {server.name}</h2>
      <dl>
        <dt>Description:</dt><dd>{server.description || '-'}</dd>
        <dt>Version:</dt><dd>{server.minecraftVersion}</dd>
        <dt>JAR URL:</dt><dd><a href={server.jarUrl}>{server.jarUrl}</a></dd>
        <dt>RAM:</dt><dd>{server.allocatedRamMB} MB</dd>
        <dt>Port:</dt><dd>{server.port}</dd>
        <dt>Running:</dt><dd>{server.running ? 'Yes' : 'No'}</dd>
        <dt>Created:</dt><dd>{new Date(server.createdAt).toLocaleString()}</dd>
      </dl>

      <div className="actions">
        <button onClick={() => setShowEdit(true)}>Edit</button>
        <button className="danger" onClick={confirmAndDelete}>Delete</button>
      </div>

      {showEdit && (
        <ServerInstanceModal
          server={server}
          isOpen={showEdit}
          onClose={() => setShowEdit(false)}
          onSaved={onUpdated}
        />
      )}
    </div>
  );
};

export default ServerSettings;