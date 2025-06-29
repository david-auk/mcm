// src/pages/users/components/EditPersonForm.tsx
import React, { useState } from 'react';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';

interface EditPersonFormProps {
    initialUsername: string;
    userId: string;
    onSaved?: () => void;
    submitting: boolean,
    setSubmitting: React.Dispatch<React.SetStateAction<boolean>>,
}

const EditPersonForm: React.FC<EditPersonFormProps> = ({
    initialUsername,
    userId,
    submitting,
    setSubmitting,
    onSaved,
}) => {
    const [username, setUsername] = useState(initialUsername);
    const [password, setPassword] = useState('');
    const toast = useToast();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        const usernameChanged = username.trim() !== initialUsername;
        const passwordProvided = password.trim() !== '';
        try {
          if (usernameChanged) {
            await authenticatedFetch.put(`/users/${userId}`, { id: userId, username: username.trim() });
          }
          if (passwordProvided) {
            await authenticatedFetch.post(`/users/change-password/${userId}`, { password });
          }
          onSaved && onSaved();
          toast('User updated successfully', 'success');
        } catch (err: any) {
          console.error(err);
          toast(err.response?.data?.error || 'Could not update user', 'error');
        } finally {
          setSubmitting(false);
          setPassword('');
        }
    };

    const isUsernameUnchanged = username.trim() === initialUsername;
    const isFormDisabled = submitting || (isUsernameUnchanged && password.trim() === '');

    return (
        <form className="form-section" onSubmit={handleSubmit}>
            <h3>Edit Details</h3>
            <label>
                Username
                <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
            </label>
            <label>
                New Password (optional)
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Leave blank to keep current password"
                />
            </label>
            <button className="btn btn--primary" type="submit" disabled={isFormDisabled}>
                Save Changes
            </button>
        </form>
    );
};

export default EditPersonForm;