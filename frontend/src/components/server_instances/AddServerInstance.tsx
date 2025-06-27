// src/pages/views/servers/components/AddServerInstanceModal.tsx
import React, { useState } from 'react';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import { useToast } from '../../contexts/ToastContext';
import Modal from '../shared/Modal';
import type ServerInstance from '../../pages/server_instance/ServerInstance';
import './AddServerInstance.css';

interface AddServerInstanceModalProps {
  onClose: () => void;
  onCreated: (server: ServerInstance) => void;
}

const AddServerInstanceModal: React.FC<AddServerInstanceModalProps> = ({
  onClose,
  onCreated,
}) => {
  const toast = useToast();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [minecraftVersion, setMinecraftVersion] = useState('');
  const [jarUrl, setJarUrl] = useState('');
  const [eulaAccepted, setEulaAccepted] = useState(false);
  const [allocatedRam, setAllocatedRam] = useState(1024);
  const [port, setPort] = useState(1024);
  const [submitting, setSubmitting] = useState(false);

  // Local port validation helper
  const getPortError = (port: number): string | null => {
    if (port < 1023 || port > 65535) {
      return 'Port must be between 1023 and 65535';
    }
    if (port % 2 !== 0) {
      return 'Port must be even';
    }
    return null;
  };

  // Validate all fields and return the first error message, or null if valid
  const getValidationError = (): string | null => {
    if (name.trim() === '') {
      return 'Server name is required.';
    }
    if (minecraftVersion.trim() === '') {
      return 'Minecraft version is required.';
    }
    if (jarUrl.trim() === '') {
      return 'Server JAR URL is required.';
    }
    if (allocatedRam <= 0) {
      return 'Allocated RAM must be greater than zero.';
    }
    const portErr = getPortError(port);
    if (portErr) {
      return portErr;
    }
    if (!eulaAccepted) {
      return 'Please accept the Minecraft EULA to proceed.';
    }
    return null;
  };

  const handleConfirm = async () => {
    const validationError = getValidationError();
    if (validationError) {
      toast(validationError, 'error');
      return;
    }
    setSubmitting(true);
    try {
      const { data } = await authenticatedFetch.post<ServerInstance>(
        '/server-instances',
        {
          name: name.trim(),
          description: description.trim() || null,
          minecraft_version: minecraftVersion.trim(),
          jar_url: jarUrl.trim(),
          eula_accepted: true,
          allocated_ram_mb: allocatedRam,
          port,
        }
      );
      toast(`Server "${name.trim()}" created`, 'success');
      onCreated(data);
    } catch (err: any) {
      console.error(err);
      toast(
        err.response?.data?.error || 'Failed to create server',
        'error'
      );
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title="Add New Server Instance"
      onClose={onClose}
      onConfirm={handleConfirm}
      confirmText={submitting ? 'Creating…' : 'Create'}
      cancelText="Cancel"
    >
      <div className="add-server-container">
        <form
          className="add-server-form"
          onSubmit={e => {
            e.preventDefault();
            handleConfirm();
          }}
        >
          <h2 className="form-title">Server Instance Details</h2>

          <fieldset className="form-section">
            <legend>Instance Info</legend>

            <label htmlFor="si-name">
              Name (unique)
              <input
                id="si-name"
                type="text"
                placeholder="e.g. MyAwesomeServer"
                value={name}
                onChange={e => setName(e.target.value)}
                required
              />
            </label>

            <label htmlFor="si-description">
              Description (optional)
              <input
                id="si-description"
                type="text"
                placeholder="Short description"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />
            </label>
          </fieldset>

          <fieldset className="form-section">
            <legend>Server Settings</legend>

            <label htmlFor="si-version">
              Minecraft Version
              <input
                id="si-version"
                type="text"
                placeholder="e.g. 1.20.1"
                value={minecraftVersion}
                onChange={e => setMinecraftVersion(e.target.value)}
                required
              />
            </label>

            <label htmlFor="si-jar">
              Server JAR URL
              <input
                id="si-jar"
                type="url"
                placeholder="https://example.com/server.jar"
                value={jarUrl}
                onChange={e => setJarUrl(e.target.value)}
                required
              />
            </label>

            <label htmlFor="si-ram">
              Allocated RAM (MB)
              <input
                id="si-ram"
                type="number"
                min={512}
                placeholder="1024"
                value={allocatedRam}
                onChange={e => setAllocatedRam(Number(e.target.value))}
                required
              />
            </label>

            <label htmlFor="si-port">
              Port (even, 1024–65535)
              <input
                id="si-port"
                type="number"
                min={1024}
                max={65534}
                step={2}
                placeholder="25565"
                value={port}
                onChange={e => setPort(Number(e.target.value))}
                required
              />
            </label>
          </fieldset>

          <fieldset className="form-section checkbox-section">
            <legend>Legal</legend>
            <label htmlFor="si-eula" className="checkbox-label">
              <input
                id="si-eula"
                type="checkbox"
                checked={eulaAccepted}
                onChange={e => setEulaAccepted(e.target.checked)}
                required
              />
              I accept the Minecraft EULA
            </label>
          </fieldset>
        </form>
      </div>
    </Modal>
  );
}

export default AddServerInstanceModal;
