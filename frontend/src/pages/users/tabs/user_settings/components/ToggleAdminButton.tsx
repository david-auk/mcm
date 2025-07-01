import React, { useState } from 'react';
import { useToast } from '../../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../../utils/auth/authenticatedFetch';
import Modal from '../../../../../components/shared/views/Modal';

interface ToggleAdminButtonProps {
  userId: string;
  isAdmin: boolean;
  /** Called after a successful toggle so parent can refresh state */
  onToggled?: () => void;
  isSelf: boolean;
  disabled: boolean;
}

const ToggleAdminButton: React.FC<ToggleAdminButtonProps> = ({
  userId,
  isAdmin,
  onToggled,
  isSelf,
  disabled,
}) => {
  const [loading, setLoading] = useState(false);
  const [confirmSelfOpen, setConfirmSelfOpen] = useState(false);
  const toast = useToast();

  const toggleAdminStatus = async () => {
    setLoading(true);
    try {
      if (!isAdmin) {
        await authenticatedFetch.post(`/users/promote/${userId}`);
        toast('User promoted to admin', 'success');
      } else {
        await authenticatedFetch.delete(`/admins/${userId}`);
        toast('Admin demoted to user', 'success');
      }
      onToggled?.();
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.error || 'Could not update admin status', 'error');
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
        className={isSelf ? "danger" : isAdmin ? "warning" : "success"}
        onClick={handleClick}
        disabled={disabled || loading}
      >
        {isAdmin ? `Revoke${isSelf ? " My ": " "}Admin Rights` : 'Grant Admin Rights'}
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
          <p>Are you sure you want to <strong>demote yourself</strong>? This cannot be undone. (by yourself)</p>
        </Modal>
      )}
    </>
  );
};

export default ToggleAdminButton;