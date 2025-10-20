import React, { useEffect, useMemo, useState } from "react";
import authenticatedFetch from "../../../../utils/auth/authenticatedFetch";
import type { RoleName } from "../../ServerInstanceView";
import Modal from "../../../../components/shared/views/Modal";

/** Minimal User shape (adjust to your API). */
interface UserRecord {
  is_admin: boolean;
  user: {
    id: string;
    username: string;
    name?: string | null;
    email?: string | null;
  };
}

interface PermissionAssignment {
  id: string;
  user: { id: string; username: string };
  serverInstance: {
    id: string;
    name: string;
    description?: string;
    minecraftVersion?: string;
    jarUrl?: string;
    eulaAccepted?: boolean;
    createdAt?: string;
    allocatedRamMB?: number;
    port?: number;
    path?: string;
    running?: boolean;
    startCommand?: string;
  };
  role: RoleName;
}

export interface UserPermissionsProps {
  /** ID of the server instance to query permissions for */
  serverInstanceId: string;
}

/**
 * Extracted control: shows a button and opens Modal to create a new permission
 * (serverId + user + role). Posts to `/api/server-instance/{id}/permissions`.
 */
const NewPermissionControl: React.FC<{
  serverInstanceId: string;
  onCreated?: () => void;
}> = ({ serverInstanceId, onCreated }) => {
  const [open, setOpen] = useState(false);
  const [users, setUsers] = useState<UserRecord[] | null>(null);
  const [usersLoading, setUsersLoading] = useState(false);
  const [usersError, setUsersError] = useState<string | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<string>("");
  const [role, setRole] = useState<RoleName>("viewer");
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  // Lazy-load users when opening the modal for the first time
  useEffect(() => {
    if (!open || users) return;
    let cancelled = false;
    setUsersLoading(true);
    setUsersError(null);
    (async () => {
      try {
        const { data } = await authenticatedFetch.get<UserRecord[]>("/users");
        if (!cancelled) setUsers(data);
      } catch (e: unknown) {
        if (!cancelled)
          setUsersError(e instanceof Error ? e.message : "Failed to load users");
      } finally {
        if (!cancelled) setUsersLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [open, users]);

  const userOptions = useMemo(
    () =>
      (users ?? []).map((r) => ({
        value: r.user.id,
        label: r.user.name ?? r.user.username ?? r.user.email ?? r.user.id,
      })),
    [users]
  );

  async function handleCreate() {
    try {
      setCreating(true);
      setCreateError(null);
      if (!selectedUserId) throw new Error("Please select a user");
      const payload = { server_instance_id: serverInstanceId, user_id: selectedUserId, role } as const;
      await authenticatedFetch.post(`/server-instance/${serverInstanceId}/permissions`, payload);
      setSelectedUserId("");
      setRole("viewer");
      setOpen(false);
      onCreated?.();
    } catch (e: unknown) {
      setCreateError(e instanceof Error ? e.message : "Failed to create permission");
    } finally {
      setCreating(false);
    }
  }

  return (
    <div>
      <button onClick={() => setOpen(true)}>New Permission</button>
      {open && Modal ? (
        <Modal
          title="New Permission"
          onClose={() => setOpen(false)}
          onConfirm={handleCreate}
          confirmText={creating ? "Creating…" : "Create Permission"}
        >
          <div style={{ marginTop: 8, display: "grid", gap: 8 }}>
            {createError && <p style={{ color: "red", margin: 0 }}>Error: {createError}</p>}

            <label>
              <span>User</span>
              {usersLoading ? (
                <span>Loading users…</span>
              ) : usersError ? (
                <span style={{ color: "red" }}>Error: {usersError}</span>
              ) : (
                <select value={selectedUserId} onChange={(e) => setSelectedUserId(e.target.value)}>
                  <option value="">— Select user —</option>
                  {userOptions.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              )}
            </label>

            <label>
              <span>Role</span>
              <select value={role} onChange={(e) => setRole(e.target.value as RoleName)}>
                {(["user", "viewer", "operator", "editor", "maintainer"] as RoleName[]).map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </Modal>
      ) : open ? (
        <div style={{ color: 'red' }}>
          Modal component was not provided. Ensure you pass your Modal via the <code>Modal</code> prop.
        </div>
      ) : null}
    </div>
  );
};

const UserPermissions: React.FC<UserPermissionsProps> = ({ serverInstanceId }) => {
  const [permissions, setPermissions] = useState<PermissionAssignment[] | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  const [editing, setEditing] = useState<PermissionAssignment | null>(null);
  const [updating, setUpdating] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);

  async function handleUpdateRole(newRole: RoleName) {
    if (!editing) return;
    try {
      setUpdating(true);
      setUpdateError(null);
      const payload = { role: newRole } as const;
      await authenticatedFetch.put(
        `/server-instance/${serverInstanceId}/permissions/${editing.id}`,
        payload
      );
      // Optimistically update local state
      setPermissions((prev) =>
        prev ? prev.map((p) => (p.id === editing.id ? { ...p, role: newRole } : p)) : prev
      );
      setEditing(null);
    } catch (e: unknown) {
      setUpdateError(e instanceof Error ? e.message : 'Failed to update role');
    } finally {
      setUpdating(false);
    }
  }

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        setLoading(true);
        setError(null);
        // NOTE: use the /api/ prefix to match backend routes
        const { data } = await authenticatedFetch.get<PermissionAssignment[]>(
          `/server-instance/${serverInstanceId}/permissions`
        );
        if (!cancelled) setPermissions(data);
      } catch (e: unknown) {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e));
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [serverInstanceId, reloadKey]);

  if (loading) return <div>Loading permissions…</div>;
  if (error) return <div style={{ color: "red" }}>Error: {error}</div>;

  return (
    <div>
      {Array.isArray(permissions) && permissions.length > 0 && (
        <div style={{ marginTop: 12 }}>
          <h4>Assignments</h4>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'center', borderBottom: '1px solid #ddd', padding: '6px' }}>User</th>
                <th style={{ textAlign: 'center', borderBottom: '1px solid #ddd', padding: '6px' }}>Role</th>
                <th style={{ textAlign: 'center', borderBottom: '1px solid #ddd', padding: '6px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {permissions.map((p) => (
                <tr key={p.id}>
                  <td style={{ padding: '6px' }}>{p.user.username}</td>
                  <td style={{ padding: '6px' }}><code>{p.role}</code></td>
                  <td style={{ padding: '6px' }}>
                    <button onClick={() => setEditing(p)}>Edit</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <hr style={{ margin: "16px 0" }} />

      {/* New Permission button + Modal */}
      <NewPermissionControl
        serverInstanceId={serverInstanceId}
        onCreated={() => setReloadKey((k) => k + 1)}
      />

      {editing && (
        <Modal
          title={`Edit Permission`}
          onClose={() => setEditing(null)}
          onConfirm={() => handleUpdateRole(editing.role)}
          confirmText={updating ? 'Saving…' : 'Save'}
        >
          <div style={{ display: 'grid', gap: 8 }}>
            {updateError && (
              <p style={{ color: 'red', margin: 0 }}>Error: {updateError}</p>
            )}
            <div>
              <strong>User:</strong> {editing.user.username}
            </div>
            <label>
              <span>Role</span>
              <select
                value={editing.role}
                onChange={(e) => setEditing({ ...editing, role: e.target.value as RoleName })}
                disabled={updating}
              >
                {(['user', 'viewer', 'operator', 'editor', 'maintainer'] as RoleName[]).map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default UserPermissions;