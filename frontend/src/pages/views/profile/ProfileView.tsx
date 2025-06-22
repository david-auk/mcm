// src/pages/views/profile/ProfileView.tsx
import React from 'react';
import SideBarView from '../../../components/shared/views/SideBarView';
import ChangeUsernameForm from './components/ChangeUsernameForm';
import ChangePasswordForm from './components/ChangePasswordForm';
import LogoutButton from './components/LogoutButton';
import DeleteAccountButton from './components/DeleteAccountButton';
import './ProfileView.css'

const ProfileView: React.FC = () => {
  const options = [
    { label: 'Personal Info', component: <ChangeUsernameForm /> },
    {
      label: 'Security',
      component: (
        <>
          <ChangePasswordForm />
          <LogoutButton />
          <DeleteAccountButton />
        </>
      ),
    },
    {
      label: 'Notifications',
      component: (
        <div>
          <h3>Notifications</h3>
          <p>Manage which emails and in-app notifications you receive. (Placeholder)</p>
        </div>
      ),
    },
  ];

  return <SideBarView title="My Profile" options={options} />;
};

export default ProfileView;
