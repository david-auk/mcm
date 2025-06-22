import React from 'react';
import TabView from '../../../components/generic/views/TabView';

const tabs = [
  {
    label: "User Management",
    path: "user-management",
    component: <p>User Management Panel (placeholder)</p>,
  },
  {
    label: "Server Overview",
    path: "servers",
    component: <p>Server Overview Panel (placeholder)</p>,
  },
];

const AdminView: React.FC = () => {
  return <TabView tabs={tabs} />;
};

export default AdminView;
