import React from 'react';
import TabView from '../../../components/shared/views/TabView';
import SystemView from './system/SystemView';
import ServerInstanceView from './server_instances/ServerInstanceView';

const tabs = [
  {
    label: "Server Overview",
    component: <ServerInstanceView />,
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
