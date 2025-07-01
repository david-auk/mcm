import React, { useState, useEffect } from 'react';
import { getUsername, isAdmin } from '../utils/auth/userDetails';
import AdminView from './views/admin/AdminView';
import ProfileView from './views/profile/ProfileView';
import TabView, { type Tab } from '../components/shared/views/TabView';
import ServerListView from '../components/server_instances/ServerListView';

const HomeScreen: React.FC = () => {
  // state to hold current username
  const [username, setUsername] = useState(getUsername());

  // listen for username updates to update the welcomeMessage
  useEffect(() => {
    const onUsernameChange = () => {
      setUsername(getUsername());
    };
    window.addEventListener('username-changed', onUsernameChange);
    return () => {
      window.removeEventListener('username-changed', onUsernameChange);
    };
  }, []);

  const welcomeMessage = `Welcome ${username}`;
  const tabs: Tab[] = [];

  if (isAdmin()) {
    tabs.push({
      label: 'Admin',
      component: <AdminView />,
    });
  } else {
    tabs.push({
      label: 'Servers',
      component: <ServerListView
        endpoint="/server-instances/me"
        allowAdd={false}
      />
    })
  }
  tabs.push({
    label: 'Account',
    component: <ProfileView />,
  });

  return (
    <main>
      {tabs.length > 0 ? (
        <TabView
          tabs={tabs}
          title="Minecraft Manager"
          subtitle={welcomeMessage}
        />
      ) : (
        <p>No tabs found... please report this as a bug.</p>
      )}
    </main>
  );
};

export default HomeScreen;
