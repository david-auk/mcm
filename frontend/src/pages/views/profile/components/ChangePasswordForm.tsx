import React, { useState } from 'react';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../../contexts/ToastContext';

const ChangePasswordForm: React.FC = () => {
  const [oldPwd, setOldPwd] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [confirm, setConfirm] = useState('');
  const toast = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPwd !== confirm) {
      toast('New passwords do not match', 'error');
      return;
    }
    try {
      await authenticatedFetch.post('/user/me/change-password', {
        old_password: oldPwd,
        new_password: newPwd,
      });
      toast('Password changed!', 'success');
      setOldPwd('');
      setNewPwd('');
      setConfirm('');
    } catch (err: any) {
      toast(err.response?.data?.message || 'Failed to change password', 'error');
    }
  };

  return (
  <form className="profile-form" onSubmit={handleSubmit}>
    <h3>Security</h3>
    <label>Current Password</label>
    <input
        id="old-pwd"
        type="password"
        value={oldPwd}
        onChange={e => setOldPwd(e.target.value)}
        required
      />
    <label>New Password</label>
    <input
        id="new-pwd"
        type="password"
        value={newPwd}
        onChange={e => setNewPwd(e.target.value)}
        required
      />
    <label>Confirm New Password</label>
    <input
        id="confirm-pwd"
        type="password"
        value={confirm}
        onChange={e => setConfirm(e.target.value)}
        required
      />
    <button type="submit">Change Password</button>
  </form>
);

  return (
    <form onSubmit={handleSubmit}>
      <h3>Security</h3>
      <label htmlFor="old-pwd">Current Password</label>
      <input
        id="old-pwd"
        type="password"
        value={oldPwd}
        onChange={e => setOldPwd(e.target.value)}
        required
      />

      <label htmlFor="new-pwd">New Password</label>
      <input
        id="new-pwd"
        type="password"
        value={newPwd}
        onChange={e => setNewPwd(e.target.value)}
        required
      />

      <label htmlFor="confirm-pwd">Confirm New Password</label>
      <input
        id="confirm-pwd"
        type="password"
        value={confirm}
        onChange={e => setConfirm(e.target.value)}
        required
      />

      <button type="submit">Change Password</button>
    </form>
  );
};

export default ChangePasswordForm;
