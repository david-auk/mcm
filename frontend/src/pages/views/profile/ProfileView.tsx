// src/pages/views/profile/ProfileView.tsx
import React from 'react';
import SideBarView from '../../../components/generic/views/SideBarView';

const ProfileView: React.FC = () => {
  const options = [
    {
      label: 'Personal Info',
      component: (
        <div>
          <h3>Personal Info</h3>
          <p>Update your display name, email address, and avatar here.</p>
          {/* insert form fields or other components as needed */}
        </div>
      ),
    },
    {
      label: 'Security',
      component: (
        <div>
          <h3>Security</h3>
          <p>Change your password or enable two-factor authentication.</p>
          {/* password change form, MFA toggles, etc. */}
        </div>
      ),
    },
    {
      label: 'Notifications',
      component: (
        <div>
          <h3>Notifications</h3>
          <p>Manage which emails and in-app notifications you receive.</p>
          {/* toggles for email alerts, push notifications, etc. */}
        </div>
      ),
    },
  ];

  return <SideBarView title="My Profile" options={options} />;
};

export default ProfileView;
