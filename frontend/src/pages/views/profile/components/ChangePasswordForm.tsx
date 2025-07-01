import React, { useState } from 'react';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import '../ProfileView.css'
import { useToast } from '../../../../contexts/ToastContext';

const ChangePasswordForm: React.FC = () => {
  const [currentPwd, setCurrentPwd] = useState('');
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
        current_password: currentPwd,
        new_password: newPwd,
      });
      toast('Password changed!', 'success');
      setCurrentPwd('');
      setNewPwd('');
      setConfirm('');
    } catch (err: any) {
      toast(err.response?.data?.error || 'Failed to change password', 'error');
    }
  };

  return (
    <form className="profile-form" onSubmit={handleSubmit}>
      <h3>Security</h3>
      <label>Current Password</label>
      <input
        id="current-pwd"
        type="password"
        value={currentPwd}
        onChange={e => setCurrentPwd(e.target.value)}
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
      {/* TODO make disabled if empty... */}
      <button className="success" type="submit">Change Password</button>
    </form>
  );
};

export default ChangePasswordForm;
