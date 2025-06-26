import React from 'react';
import TabView from '../../../components/shared/views/TabView';
import SystemView from './system/SystemView';
import ServerListView from '../../../components/server_instances/ServerListView';

const tabs = [
  {
    label: "Server Overview",
    component: <ServerListView
      endpoint="/server-instances"
      allowAdd={true}
    />,
  },
  {
    label: "User Management",
    component: <p>User Management Panel (placeholder)</p>,
  },
  {
    label: "System",
    component: <SystemView />,
  }
];

const AdminView: React.FC = () => {
  return <TabView tabs={tabs} />;
};

export default AdminView;
