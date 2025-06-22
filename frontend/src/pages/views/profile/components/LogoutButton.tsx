import React from 'react';
import { useNavigate } from 'react-router-dom';
import { removeToken } from '../../../../utils/auth/token';
import { clearUserDetails } from '../../../../utils/auth/userDetails';
import { useToast } from '../../../../contexts/ToastContext';

const LogoutButton: React.FC = () => {
    const navigate = useNavigate();
    const toast = useToast();

    const handleLogout = () => {
        removeToken();
        clearUserDetails();
        toast('Logged out', 'success');
        navigate('/login');
    };

    return (
        <div className="profile-security-buttons">
            <button className="logout" onClick={handleLogout}>Logout</button>
        </div >
    )

};

export default LogoutButton;
