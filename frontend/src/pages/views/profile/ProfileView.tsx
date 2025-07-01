// src/pages/views/profile/ProfileView.tsx
import React from 'react';
import SideBarView from '../../../components/shared/views/SideBarView';
import ChangeUsernameForm from './components/ChangeUsernameForm';
import ChangePasswordForm from './components/ChangePasswordForm';
import LogoutButton from './components/LogoutButton';
import DeleteAccountButton from './components/DeleteAccountButton';
import './ProfileView.css'
import NotificationsList from '../../../components/shared/notifications/NotificationsList';

const ProfileView: React.FC = () => {
  const options = [
    { label: 'Personal Info', component: <ChangeUsernameForm /> },
    {
      label: 'Security',
      component: (
        <>
          <div className='security'>
            <ChangePasswordForm />
            <LogoutButton />
            <DeleteAccountButton />
          </div>
        </>
      ),
    },
    {
      label: 'Notifications',
      component: <NotificationsList endpoint='/user/me/notifications' />,
    },
  ];

  return <SideBarView title="My Profile" options={options} />;
};

export default ProfileView;
