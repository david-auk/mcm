// src/pages/views/profile/components/DeleteAccountButton.tsx
import React, { useState } from 'react';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../../contexts/ToastContext';
import { useNavigate } from 'react-router-dom';
import { removeToken } from '../../../../utils/auth/token';
import { clearUserDetails } from '../../../../utils/auth/userDetails';
import Modal from '../../../../components/shared/Modal';

const DeleteAccountButton: React.FC = () => {
    const [open, setOpen] = useState(false);
    const toast = useToast();
    const navigate = useNavigate();

    const confirmDelete = async () => {
        try {
            await authenticatedFetch.delete('/user/me');
            removeToken();
            clearUserDetails();
            toast('Account deleted', 'success');
            navigate('/login');
        } catch (err: any) {
            setOpen(false)
            toast(err.response?.data?.message || 'Deletion failed', 'error');
        }
    };

    return (
        <div>
            <button className="delete" onClick={() => setOpen(true)}>Delete Account</button>
            {open && (
                <Modal
                    title="Confirm Account Deletion"
                    onClose={() => setOpen(false)}
                    onConfirm={confirmDelete}
                    confirmText="Delete"
                    cancelText="Cancel"
                >
                    <p>Are you sure you want to <strong>permanently</strong> delete your account? This cannot be undone.</p>
                </Modal>
            )}
        </div>
    )
};

export default DeleteAccountButton;
