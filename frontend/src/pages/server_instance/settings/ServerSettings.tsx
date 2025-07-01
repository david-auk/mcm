// src/components/server_instances/ServerSettings.tsx
import React, { useState } from 'react';
import './ServerSettings.css';
import type ServerInstance from '../ServerInstance';
import { useToast } from '../../../contexts/ToastContext';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import ServerInstanceModal from '../../../components/server_instances/ServerInstanceModal';
import Modal from '../../../components/shared/views/Modal';
import type { NavigateFunction } from 'react-router-dom';

interface Props {
  server: ServerInstance;
  navigate: NavigateFunction;
  onUpdated: (updated: ServerInstance) => void;
}

const ServerSettings: React.FC<Props> = ({ server, navigate, onUpdated }) => {
  const toast = useToast();
  const [showEdit, setShowEdit] = useState(false);
  const [showDelete, setShowDelete] = useState(false);

  const deleteInstance = async () => {
    try {
      await authenticatedFetch.delete(`/server-instances/${server.id}`);
      toast(`Deleted ${server.name}`, 'success');
      navigate("/home")
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
        <button className="secondary" onClick={() => setShowEdit(true)}>Edit</button>
        <button className="danger" onClick={() => setShowDelete(true)}>Delete</button>
      </div>

      {showEdit && (
        <ServerInstanceModal
          server={server}
          isOpen={showEdit}
          onClose={() => setShowEdit(false)}
          onSaved={onUpdated}
        />
      )}

      {showDelete && (
        <Modal
          title="Delete Server Instance"
          onClose={() => setShowDelete(false)}
          onConfirm={deleteInstance}
          confirmText='Delete'
          children={
            <p>
              Are you sure you want to <strong>permanently</strong> delete <strong>{server.name}</strong>? This cannot be
              undone.
            </p>

          }
        />
      )}
    </div>
  );
};

export default ServerSettings;