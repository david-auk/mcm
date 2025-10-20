import React, { useState } from 'react';
import './ServerSettings.css';
import type ServerInstance from '../../ServerInstance';
import { useToast } from '../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import ServerInstanceModal from '../../../../components/server_instances/ServerInstanceModal';
import Modal from '../../../../components/shared/views/Modal';
import type { NavigateFunction } from 'react-router-dom';

// no new external imports required

interface Props {
  server: ServerInstance;
  navigate: NavigateFunction;
  onUpdated: (updated: ServerInstance) => void;
}

const ServerSettings: React.FC<Props> = ({ server, navigate, onUpdated }) => {
  const toast = useToast();
  const [showEdit, setShowEdit] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [showExportInfo, setShowExportInfo] = useState(false);

  const deleteInstance = async () => {
    try {
      await authenticatedFetch.delete(`/server-instances/${server.id}`);
      toast(`Deleted ${server.name}`, 'success');
      navigate("/home")
    } catch (e: any) {
      toast(e.response?.data?.error || 'Delete failed', 'error');
    }
  };

  const exportInstance = async () => {
    setExporting(true);
    try {
      // Request the export as a binary blob
      const res = await authenticatedFetch.get(`/server-instances/${server.id}/export`, {
        responseType: 'blob'
      });

      const blob = new Blob([res.data], { type: 'application/zip' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'export.zip';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      toast('Export created successfully', 'success');
      setShowExportInfo(false);
    } catch (e: any) {
      // Try to extract a readable error if backend sent JSON instead of a blob
      if (e?.response?.data) {
        try {
          const text = await (e.response.data.text ? e.response.data.text() : Promise.resolve(''));
          if (text) {
            const parsed = JSON.parse(text);
            toast(parsed?.error || 'Export failed', 'error');
            return;
          }
        } catch (_) {
          // ignore JSON parse errors, fall through to generic
        }
      }
      toast(e.response?.data?.error || 'Export failed', 'error');
    } finally {
      setExporting(false);
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
        <button
          className="holo"
          onClick={() => setShowExportInfo(true)}
          disabled={exporting}
        >
          {exporting ? (
            'Exporting…'
          ) : (
            <>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 3l-4 4h3v4h2V7h3l-4-4z" />
                <path d="M5 13v6h14v-6h2v8H3v-8h2z" />
              </svg>
              Export
            </>
          )}
        </button>
      </div>

      {showEdit && (
        <ServerInstanceModal
          server={server}
          isOpen={showEdit}
          onClose={() => setShowEdit(false)}
          onSaved={onUpdated} 
          forumMode={'edit'} />
      )}

      {showExportInfo && (
        <Modal
          title="Export Server Instance"
          onClose={() => setShowExportInfo(false)}
          onConfirm={async () => {
            if (exporting) return; // prevent double clicks
            await exportInstance();
            // Close the modal after export attempt; success or error toast will inform user.
            setShowExportInfo(false);
          }}
          confirmText={exporting ? 'Exporting…' : 'Download export.zip'}
        >
          <div>
            <p>This will create a <code>export.zip</code> for <strong>{server.name}</strong>.</p>
            <h4>Included in the export</h4>
            <ul>
              <li>All folder contents of the server instance directory (the world, configs, logs, etc.).</li>
              <li><code>mcm_metadata.json</code> — serialized metadata of the server instance.</li>
            </ul>
            <h4>Not included</h4>
            <ul>
              <li>User data (accounts, activity, etc.).</li>
              <li>User permissions/access rights to this server instance.</li>
            </ul>
            <p>You can import this archive elsewhere, but you’ll need to recreate user permissions separately.</p>
          </div>
        </Modal>
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