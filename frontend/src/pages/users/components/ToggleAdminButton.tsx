import React, { useState } from 'react';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import Modal from '../../../components/shared/Modal';

interface ToggleAdminButtonProps {
  userId: string;
  isAdmin: boolean;
  /** Called after a successful toggle so parent can refresh state */
  onToggled?: () => void;
  isSelf: boolean;
}

const ToggleAdminButton: React.FC<ToggleAdminButtonProps> = ({
  userId,
  isAdmin,
  onToggled,
  isSelf,
}) => {
  const [loading, setLoading] = useState(false);
  const [confirmSelfOpen, setConfirmSelfOpen] = useState(false);
  const toast = useToast();

  const toggleAdminStatus = async () => {
    setLoading(true);
    try {
      if (!isAdmin) {
        await authenticatedFetch.post(`/users/promote/${userId}`);
        toast('User granted admin rights', 'success');
      } else {
        await authenticatedFetch.delete(`/admins/${userId}`);
        toast('User admin rights revoked', 'success');
      }
      onToggled?.();
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.message || 'Could not update admin status', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleClick = async () => {
    if (isAdmin && isSelf) {
      setConfirmSelfOpen(true);
      return;
    }
    await toggleAdminStatus();
  };

  return (
    <>
      <button
        className="btn btn--primary"
        onClick={handleClick}
        disabled={loading}
      >
        {isAdmin ? 'Revoke Admin Rights' : 'Grant Admin Rights'}
      </button>

      {/* Self-demotion confirmation */}
      {confirmSelfOpen && (
        <Modal
          title="Confirm Self-Demotion"
          onClose={() => setConfirmSelfOpen(false)}
          onConfirm={() => {
            setConfirmSelfOpen(false);
            toggleAdminStatus();
          }}
          confirmText="Demote Me"
          cancelText="Cancel"
        >
          <p>Are you sure you want to <strong>demote yourself</strong>? This cannot be undone.</p>
        </Modal>
      )}
    </>
  );
};

export default ToggleAdminButton;