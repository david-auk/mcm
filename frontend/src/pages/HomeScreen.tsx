import React from 'react';
import TabView from '../components/generic/views/TabView';
import { getUsername, isAdmin } from '../utils/auth/userDetails';
import AdminView from './views/admin/AdminView';
import ProfileView from './views/profile/ProfileView';

interface Tab {
  label: string;
  path: string;
  component: React.ReactNode;
}

const HomeScreen: React.FC = () => {
  const welcomeMessage = `Welcome ${getUsername()}`;

  const tabs: Tab[] = [];

  if (isAdmin()) {
    tabs.push({
      label: "Admin View",
      path: 'admin', // path is relative to /home
      component: <AdminView />,
    });
  }

  tabs.push({
    label: "Account",
    path: "profile",
    component: <ProfileView />,
  });

  return (
    <main>
      {tabs.length > 0 ? (
        <TabView
          tabs={tabs}
          title={welcomeMessage}
        />
      ) : (
        <p>No tabs found... please report this as a bug.</p>
      )}
    </main>
  );
};

export default HomeScreen;
