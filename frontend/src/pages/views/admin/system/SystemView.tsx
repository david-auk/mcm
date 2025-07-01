import React from 'react';
import TabView from '../../../../components/shared/views/TabView';

const tabs = [
  {
    label: "Notifications",
    component: <p>Server Notifications Overview (placeholder)</p>,
  },
  {
    label: "Settings",
    component: <p>Settings Panel (placeholder)</p>,
  }
];

const SystemView: React.FC = () => {
  return <TabView tabs={tabs} />;
};

export default SystemView;
