// src/components/server_instances/ServerInstanceModal.tsx
import React, { useState, useEffect } from 'react';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import { useToast } from '../../contexts/ToastContext';
import Modal from '../shared/views/Modal';
import type ServerInstance from '../../pages/server_instance/ServerInstance';
import type { AxiosResponse } from 'axios';

interface Props {
  server?: ServerInstance;
  isOpen: boolean;
  onClose: () => void;
  onSaved: (instance: ServerInstance) => void;
  forumMode: 'new' | 'edit' | 'import'
  importedFile?: File | null
}

const ServerInstanceModal: React.FC<Props> = ({
  server,
  onClose,
  onSaved,
  forumMode,
  importedFile
}) => {
  const toast = useToast();
  // const isEdit = Boolean(server);

  // initialize state from either blank (create) or existing
  const [name, setName] = useState(server?.name ?? '');
  const [description, setDescription] = useState(server?.description ?? '');
  const [minecraftVersion, setMinecraftVersion] = useState(server?.minecraftVersion ?? '');
  const [jarUrl, setJarUrl] = useState(server?.jarUrl ?? '');
  const [eulaAccepted, setEulaAccepted] = useState(false);
  const [allocatedRam, setAllocatedRam] = useState(server?.allocatedRamMB ?? 1024);
  const [port, setPort] = useState(server?.port ?? 1024);
  const [submitting, setSubmitting] = useState(false);

  let title: string

  switch (forumMode) {
    case 'new':
      title = "Add New Server Instance";
      break;
    case 'edit':
      title = "Edit Server Instance";
      break;
    case 'import':
      title = "Import Server Instance";
      break;
  }

  // keep form in sync if server prop changes
  useEffect(() => {
    if (server) {
      setName(server.name);
      setDescription(server.description ?? '');
      setMinecraftVersion(server.minecraftVersion);
      setJarUrl(server.jarUrl);
      setEulaAccepted(forumMode === "edit" || forumMode === 'import'); // Because in order to edit you need to have accepted.
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
    if (submitting) return;
    const err = getValidationError();
    if (err) {
      toast(err, 'error');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        ...(forumMode === 'edit' ? { id: server!.id } : {}), // Add optional id when the ID is known.
        name: name.trim(),
        description: description.trim() || null,
        minecraft_version: minecraftVersion.trim(),
        jar_url: jarUrl.trim(),
        eula_accepted: eulaAccepted,
        allocated_ram_mb: allocatedRam,
        port,
      };

      let res: AxiosResponse;
      let message: string;

      switch (forumMode) {
        case 'new':
          res = await authenticatedFetch.post<ServerInstance>(
            "/server-instances",
            payload
          );
          message = "created";
          break;
        case 'edit':
          res = await authenticatedFetch.put<ServerInstance>(
            `/server-instances/${server!.id}`,
            payload
          );
          message = "updated";
          break;
        case 'import':
          { if (!importedFile || !server) {
            toast('Missing data to finalize import', 'error');
            return;
          }
          const form = new FormData();
          form.append('file', importedFile);
          form.append('server', new Blob([JSON.stringify(server)], { type: 'application/json' }));
          res = await authenticatedFetch.post('/server-instances/import', form, {
            headers: { 'Content-Type': 'multipart/form-data' },
          } as never);
          message = "imported"; }
      }

      toast(`Server ${message} successfully`, 'success');
      onSaved(res.data);
      onClose();
    } catch (e: any) {
      toast(e.response?.data?.error || 'Save failed', 'error');
      setSubmitting(false);
    }
  }

  return (
    <Modal
      title={title}
      onClose={onClose}
      onConfirm={handleSave}
      confirmText={forumMode === 'new' ? "Create" : 'Save'}
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
                disabled={
                  forumMode === 'edit' && server?.eulaAccepted || forumMode === 'import' && (jarUrl) !== ""
                }
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