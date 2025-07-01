import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './UserView.css';
import { useToast } from '../../contexts/ToastContext';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import TabView from '../../components/shared/views/TabView';
import { getUserId, getUsername } from '../../utils/auth/userDetails';
import UserSettings from './tabs/user_settings/UserSettings';

export interface UserResponse {
  user: {
    id: string;
    username: string;
  };
  is_admin: boolean;
}

const UserView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<UserResponse | null>(null);

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

  const isViewingSelf = getUserId()! === id;

  const tabs = [
    {
      label: 'User',
      component: <UserSettings
        user={user.user}
        fetchUser={fetchUser}
        isAdmin={user.is_admin}
        isSelf={isViewingSelf}
        navigate={navigate} />
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
        subtitle={isViewingSelf ? "Logged In" : `ID: ${id}`}
      />
    </main>
  );
};

export default UserView;