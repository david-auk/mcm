// src/pages/views/admin/user_management/UserManagementView.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './UserManagementView.css';
import { useToast } from '../../../../contexts/ToastContext';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import AddUserModal from './AddUserModal';

interface User {
  user: {
    id: string;
    username: string;
  };
  is_admin: boolean;
}

const UserManagementView: React.FC = () => {
  const [users, setUsers]     = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const toast                 = useToast();
  const navigate              = useNavigate();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const { data } = await authenticatedFetch.get<User[]>('/users');
      setUsers(data);
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.message || 'Could not load users', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return (
    <div className="user-list-container">
      <header className="user-list-header">
        <h2>Users</h2>

        {/* This view always allows adding users */}
        <button
          className="secondary"
          onClick={() => setShowAdd(true)}
        >
          + Add User
        </button>
      </header>

      {loading ? (
        <p>Loading users…</p>
      ) : users.length === 0 ? (
        <p>No users found. Click “Add User” to get started.</p>
      ) : (
        <ul className="user-list">
          {users.map(u => (
            <li
              key={u.user.id}
              className="user-list__item"
              onClick={() => navigate(`/user/${u.user.id}`)}
            >
              <span className="user-name">{u.user.username}</span>
              {u.is_admin && <span className="badge badge--admin">Admin</span>}
            </li>
          ))}
        </ul>
      )}

      {showAdd && (
        <AddUserModal
          isOpen={showAdd}
          onClose={() => setShowAdd(false)}
          onSaved={() => {
            setShowAdd(false);
            fetchUsers();
          }}
        />
      )}
    </div>
  );
};

export default UserManagementView;