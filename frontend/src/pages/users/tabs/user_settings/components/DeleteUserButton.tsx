// src/pages/users/DeleteUserButton.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../../utils/auth/authenticatedFetch';
import Modal from '../../../../../components/shared/views/Modal';

interface DeleteUserButtonProps {
    userId: string;
    disabled?: boolean;
    isSelf: boolean;
    onDeleted?: () => void;
}

const DeleteUserButton: React.FC<DeleteUserButtonProps> = ({
    userId,
    disabled = false,
    onDeleted,
    isSelf,
}) => {
    const [open, setOpen] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const toast = useToast();
    const navigate = useNavigate();

    const handleConfirm = async () => {
        setSubmitting(true);
        try {
            await authenticatedFetch.delete(`/users/${userId}`);
            toast('User deleted successfully', 'success');
            setOpen(false);
            onDeleted ? onDeleted() : navigate('/home');
        } catch (err: any) {
            console.error(err);
            toast(err.response?.data?.message || 'Could not delete user', 'error');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <>
            <button
                className="danger"
                onClick={() => setOpen(true)}
                disabled={disabled || submitting}
            >
                Delete {isSelf ? "Me" : "User"}
            </button>
            {open && (
                <Modal
                    title={isSelf ? "Confirm Self-Deletion" : "Confirm User Deletion"}
                    onClose={() => setOpen(false)}
                    onConfirm={handleConfirm}
                    confirmText={isSelf ? "Delete Me" : "Delete"}
                    cancelText="Cancel"
                >
                    <p>
                        Are you sure you want to <strong>permanently</strong> delete {isSelf ? "the user you are currently logged in as" : "this user"}? This cannot be
                        undone.
                    </p>

                </Modal>
            )}
        </>
    );
};

export default DeleteUserButton;