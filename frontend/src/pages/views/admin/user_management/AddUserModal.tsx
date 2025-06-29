// src/pages/views/admin/user_management/AddUserModal.tsx
import React, { useEffect, useState } from 'react';
import './AddUserModal.css';
import { useToast } from '../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import Modal from '../../../../components/shared/Modal';

interface User {
  id: string;
  username: string;
}

interface Props {
  /** Provide a user when editing –- omit for “add new” */
  user?: User;
  isOpen: boolean;
  onClose: () => void;
  /** Callback with freshly created / updated user */
  onSaved: (user: User) => void;
}

const AddUserModal: React.FC<Props> = ({ user, isOpen, onClose, onSaved }) => {
  const toast = useToast();
  const isEdit = Boolean(user);

  /* ───────────────────  form state  ─────────────────── */
  const [username, setUsername] = useState(user?.username ?? '');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // keep form in sync if parent swaps the `user` prop
  useEffect(() => {
    setUsername(user?.username ?? '');
    setPassword('');
  }, [user]);

  /* ───────────────────  validation  ─────────────────── */
  const getValidationError = (): string | null => {
    if (!username.trim()) return 'Username is required.';
    if (!isEdit && !password) return 'Password is required.';
    return null;
  };

  /* ───────────────────  save handler  ─────────────────── */
  const handleSave = async () => {
    const err = getValidationError();
    if (err) {
      toast(err, 'error');
      return;
    }

    setSubmitting(true);
    try {
      const payload: Record<string, unknown> = { username: username.trim() };
      if (password) payload.password = password; // backend expects “password”

      const res = isEdit
        ? await authenticatedFetch.put<User>(`/users/${user!.id}`, payload)
        : await authenticatedFetch.post<User>('/users', payload);

      toast(`User ${isEdit ? 'updated' : 'created'} successfully`, 'success');
      onSaved(res.data);
      onClose();
    } catch (e: any) {
      toast(e.response?.data?.error || 'Save failed', 'error');
      setSubmitting(false);
    }
  };

  /* ───────────────────  no render when closed  ─────────────────── */
  if (!isOpen) return null;

  /* ───────────────────  UI  ─────────────────── */
  return (
    <Modal
      title={isEdit ? 'Edit User' : 'Add New User'}
      onClose={onClose}
      onConfirm={handleSave}
      confirmText={
        submitting
          ? isEdit
            ? 'Saving…'
            : 'Creating…'
          : isEdit
          ? 'Save'
          : 'Create'
      }
      cancelText="Cancel"
    >
      <div className="user-modal__container">
        <form
          className="user-form"
          onSubmit={e => {
            e.preventDefault();
            handleSave();
          }}
        >
          <h2 className="form-title">User Details</h2>

          <fieldset className="form-section">
            <legend>Account Info</legend>

            <label htmlFor="um-username">
              Username
              <input
                id="um-username"
                type="text"
                placeholder="johndoe"
                value={username}
                onChange={e => setUsername(e.target.value)}
                required
              />
            </label>

            <label htmlFor="um-password">
              {isEdit
                ? 'New Password (leave blank to keep current)'
                : 'Password'}
              <input
                id="um-password"
                type="password"
                placeholder={
                  isEdit ? '••••••••' : 'Choose a strong password'
                }
                value={password}
                onChange={e => setPassword(e.target.value)}
                {...(isEdit ? {} : { required: true })}
              />
            </label>
          </fieldset>
        </form>
      </div>
    </Modal>
  );
};

export default AddUserModal;