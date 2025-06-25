// src/pages/views/servers/components/AddServerInstanceModal.tsx
import React, { useState } from 'react';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../../contexts/ToastContext';
import Modal from '../../../../components/shared/Modal';

export interface ServerInstance {
  id: string;
  name: string;
  description?: string;
  minecraft_version: string;
  jar_url: string;
  eula_accepted: boolean;
  created_at: string;
  allocated_ram_mb: number;
  port: number;
}

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

  const isValid =
    name.trim() !== '' &&
    minecraftVersion.trim() !== '' &&
    jarUrl.trim() !== '' &&
    eulaAccepted &&
    port >= 1024 &&
    port <= 65535 &&
    port % 2 === 0 &&
    allocatedRam > 0;

  const handleConfirm = async () => {
    if (!isValid) return;
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
      toast(`Server "${data.name}" created`, 'success');
      onCreated(data);
    } catch (err: any) {
      console.error(err);
      toast(
        err.response?.data?.message || 'Failed to create server',
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
      <form
        className="add-server-form"
        onSubmit={e => {
          e.preventDefault();
          handleConfirm();
        }}
      >
        <label htmlFor="si-name">
          Name (unique)
          <input
            id="si-name"
            type="text"
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
            value={description}
            onChange={e => setDescription(e.target.value)}
          />
        </label>

        <label htmlFor="si-version">
          Minecraft Version
          <input
            id="si-version"
            type="text"
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
            max={65535}
            step={2}
            value={port}
            onChange={e => setPort(Number(e.target.value))}
            required
          />
        </label>

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
      </form>
    </Modal>
  );
};

export default AddServerInstanceModal;
