import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import InitializationPage from './initalization/InitializationPage';
import type ServerInstance from './ServerInstance';
import './ServerInstanceView.css';
import { useToast } from '../../contexts/ToastContext';
import { isAdmin } from '../../utils/auth/userDetails';
import authenticatedFetch from '../../utils/auth/authenticatedFetch';
import type { Tab } from '../../components/shared/views/TabView';
import TabView from '../../components/shared/views/TabView';
import Dashboard from './dashboard/Dashboard';
import Console from './console/Console';
import ServerSettings from './settings/ServerSettings';

type RoleName = 'user' | 'viewer' | 'operator' | 'editor' | 'maintainer';

interface Role {
  name: RoleName;
  description: string;
}

const ServerInstanceView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [server, setServer] = useState<ServerInstance | null>(null);
  const [loading, setLoading] = useState(true);
  const [roles, setRoles] = useState<Role[]>([]);
  const [rolesLoading, setRolesLoading] = useState(false);

  const toast = useToast();
  const navigate = useNavigate();
  const admin = isAdmin();

  // 1) Load the server
  useEffect(() => {
    if (!id) return;
    setLoading(true);
    authenticatedFetch
      .get<ServerInstance>(`/server-instances/${id}`)
      .then(({ data }) => setServer(data))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || 'Failed to load server', 'error');
        navigate('/servers');
      })
      .finally(() => setLoading(false));
  }, [id, navigate, toast]);

  // 2) If non-admin and initialized, fetch roles once
  useEffect(() => {
    if (!server || !server.eulaAccepted || admin) return;
    setRolesLoading(true);
    authenticatedFetch
      .get<Role[]>(`/server-instances/me/${id}/roles`)
      .then(({ data }) => setRoles(data))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || 'Failed to load roles', 'error');
      })
      .finally(() => setRolesLoading(false));
  }, [server, id, admin, toast]);

  if (loading) return <p>Loading server…</p>;
  if (!server) return null;

  // 3) Build tabs
  const tabs: Tab[] = [];

  const allowedToView = (roleName: RoleName) => {

    if (admin) return true;

    return (roles as Role[]).some(r => r.name === roleName);
  }

  // If not initialized and user is admin, show only Initialize
  if (!server.eulaAccepted && admin) {
    tabs.push({
      label: 'Initialize',
      component: <InitializationPage serverInstance={server} />,
    });
  } else {
    // Always show Dashboard (or Overview)
    tabs.push({
      label: 'Dashboard',
      component: <Dashboard server={server} />,
    });

    // If initialized:
    if (server.eulaAccepted) {
      // While roles are loading:
      if (!admin && rolesLoading) {
        tabs.push({
          label: 'Loading…',
          component: <p>Loading your permissions…</p>,
        });
      } else {
        if (allowedToView('viewer')) tabs.push({
          label: 'Console',
          component: (
            <Console
              isOperator={allowedToView('operator')} // or roles.includes('operator') || isAdmin()
            />
          ),
        })
        if (allowedToView('editor')) tabs.push({
          label: "Propperties",
          component: <p>Placeholder</p>
        })
        if (allowedToView('maintainer')) {
          tabs.push({
            label: 'Settings',
            component: (
              <ServerSettings
                server={server!}
                onDeleted={() => {
                  // after deletion, navigate back to list
                  navigate('/home');
                }}
                onUpdated={(updated: ServerInstance) => {
                  // update local state so view reflects changes
                  setServer(updated);
                }}
              />
            ),
          });
        }
      }
    }
  }

  return (
    <main>
      <TabView
        tabs={tabs}
        title={server.name}
        subtitle={
          server.eulaAccepted
            ? server.description
            : 'Not initialized'
        }
      />
    </main>
  );
};

export default ServerInstanceView;
