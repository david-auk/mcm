import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './UserView.css';
import { useToast } from '../../contexts/ToastContext';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import TabView from '../../components/shared/views/TabView';
import EditPersonForm from './components/EditPersonForm';
import DeleteUserButton from './components/DeleteUserButton';
import ToggleAdminButton from './components/ToggleAdminButton';
import { getUsername } from '../../utils/auth/userDetails';

interface UserResponse {
  user: {
    id: string;
    username: string;
  };
  is_admin: boolean;
}

const UserView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<UserResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const toast = useToast();
  const navigate = useNavigate();

  const fetchUser = async () => {
    try {
      const { data } = await authenticatedFetch.get<UserResponse>(`/users/${id}`);
      setUser(data);
    } catch (err: any) {
      console.error(err);
      toast(err.response?.data?.error || 'Could not load user', 'error');
      navigate('/home');
    }
  };

  useEffect(() => {
    fetchUser();
  }, [id]);

  if (!user) {
    return <p>Loading user details…</p>;
  }

  const isViewingSelf = getUsername() === user.user.username;

  const tabs = [
    {
      label: 'User Settings',
      component: (
        <section className="user-settings">
          <div className="user-actions">
            <ToggleAdminButton
              userId={user.user.id}
              isAdmin={user.is_admin}
              isSelf={isViewingSelf}
              onToggled={fetchUser}
            />
            <DeleteUserButton
              userId={user.user.id}
              disabled={submitting}
              onDeleted={() => navigate('/home')}
            />
          </div>
          <EditPersonForm
            initialUsername={user.user.username}
            userId={user.user.id}
            onSaved={fetchUser}
          />
        </section>
      ),
    },
    {
      label: 'Permissions',
      component: <p>test</p>, // <ServerPermissions userId={user.user.id} />
    },
  ];

  return (
    <main>
      <TabView
        tabs={tabs}
        title={user.user.username}
        subtitle={`ID: ${id}`}
      />
    </main>
  );
};

export default UserView;