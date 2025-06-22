import React from 'react';
import TabView from '../../../components/generic/views/TabView';

const tabs = [
  {
    label: "User Management",
    component: <p>User Management Panel (placeholder)</p>,
  },
  {
    label: "Server Overview",
    component: <p>Server Overview Panel (placeholder)</p>,
  },
];

const AdminView: React.FC = () => {
  return <TabView tabs={tabs} />;
};

export default AdminView;
