// src/components/server_instances/ServerInstanceModal.tsx
import React, { useState, useEffect } from 'react';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import { useToast } from '../../contexts/ToastContext';
import Modal from '../shared/views/Modal';
import type ServerInstance from '../../pages/server_instance/ServerInstance';

interface Props {
  server?: ServerInstance;
  isOpen: boolean;
  onClose: () => void;
  onSaved: (instance: ServerInstance) => void;
}

const ServerInstanceModal: React.FC<Props> = ({
  server,
  onClose,
  onSaved,
}) => {
  const toast = useToast();
  const isEdit = Boolean(server);

  // initialize state from either blank (create) or existing
  const [name, setName] = useState(server?.name ?? '');
  const [description, setDescription] = useState(server?.description ?? '');
  const [minecraftVersion, setMinecraftVersion] = useState(server?.minecraftVersion ?? '');
  const [jarUrl, setJarUrl] = useState(server?.jarUrl ?? '');
  const [eulaAccepted, setEulaAccepted] = useState(server?.eulaAccepted ?? false);
  const [allocatedRam, setAllocatedRam] = useState(server?.allocatedRamMB ?? 1024);
  const [port, setPort] = useState(server?.port ?? 1024);
  const [submitting, setSubmitting] = useState(false);

  // keep form in sync if server prop changes
  useEffect(() => {
    if (server) {
      setName(server.name);
      setDescription(server.description ?? '');
      setMinecraftVersion(server.minecraftVersion);
      setJarUrl(server.jarUrl);
      setEulaAccepted(server.eulaAccepted);
      setAllocatedRam(server.allocatedRamMB);
      setPort(server.port);
    }
  }, [server]);

  const getPortError = (p: number): string | null => {
    if (p < 1024 || p > 65535) {
      return 'Port must be between 1024 and 65535';
    }
    if (p % 2 !== 0) {
      return 'Port must be even';
    }
    return null;
  };

  const getValidationError = (): string | null => {
    if (!name.trim()) return 'Server name is required.';
    if (!minecraftVersion.trim()) return 'Minecraft version is required.';
    if (!jarUrl.trim()) return 'Server JAR URL is required.';
    if (allocatedRam <= 0) return 'Allocated RAM must be greater than zero.';
    const portErr = getPortError(port);
    if (portErr) return portErr;
    if (!eulaAccepted) return 'Please accept the Minecraft EULA.';
    return null;
  };

  const handleSave = async () => {
    const err = getValidationError();
    if (err) {
      toast(err, 'error');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        name: name.trim(),
        description: description.trim() || null,
        minecraft_version: minecraftVersion.trim(),
        jar_url: jarUrl.trim(),
        eula_accepted: eulaAccepted,
        allocated_ram_mb: allocatedRam,
        port,
      };

      const res = isEdit
        ? await authenticatedFetch.put<ServerInstance>(`/server-instances/${server!.id}`, payload)
        : await authenticatedFetch.post<ServerInstance>('/server-instances', payload);

      toast(`Server ${isEdit ? 'updated' : 'created'} successfully`, 'success');
      onSaved(res.data);
      onClose();
    } catch (e: any) {
      toast(e.response?.data?.error || 'Save failed', 'error');
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={isEdit ? 'Edit Server Instance' : 'Add New Server Instance'}
      onClose={onClose}
      onConfirm={handleSave}
      confirmText={isEdit ? "Save" : 'Create'}
      cancelText="Cancel"
    >
      <div>
        <form
          onSubmit={e => {
            e.preventDefault();
            handleSave();
          }}
        >
          <h2>Server Instance Details</h2>

          {/* TODO modular */}
          <fieldset> 
            <legend>Instance Info</legend>

            <label>
              Name (unique)
              <input
                type="text"
                placeholder="e.g. MyAwesomeServer"
                value={name}
                onChange={e => setName(e.target.value)}
                required
              />
            </label>

            <label>
              Description (optional)
              <input
                type="text"
                placeholder="Short description"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />
            </label>
          </fieldset>

          <fieldset>
            <legend>Server Settings</legend>

            <label>
              Minecraft Version
              <input
                type="text"
                placeholder="e.g. 1.20.1"
                value={minecraftVersion}
                onChange={e => setMinecraftVersion(e.target.value)}
                required
              />
            </label>

            <label>
              Server JAR URL
              <input
                type="url"
                placeholder="https://example.com/server.jar"
                value={jarUrl}
                onChange={e => setJarUrl(e.target.value)}
                required
              />
            </label>

            <label>
              Allocated RAM (MB)
              <input
                type="number"
                min={512}
                placeholder="1024"
                value={allocatedRam}
                onChange={e => setAllocatedRam(Number(e.target.value))}
                required
              />
            </label>

            <label>
              Port (even, 1024–65535)
              <input
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

          <fieldset>
            <legend>Legal</legend>
            <label>
              <input
                type="checkbox"
                checked={eulaAccepted}
                onChange={e => setEulaAccepted(e.target.checked)}
                required
              />
              I accept the{' '}
              <a href="https://aka.ms/MinecraftEULA" target="_blank" rel="noopener noreferrer">
                Minecraft EULA
              </a>
            </label>
          </fieldset>
        </form>
      </div>
    </Modal>
  );
};

export default ServerInstanceModal;