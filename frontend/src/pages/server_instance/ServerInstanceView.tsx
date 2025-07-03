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
  const [isInitializing, setIsInitializing] = useState(false);

  const toast = useToast();
  const navigate = useNavigate();
  const admin = isAdmin();

  const loadServer = () => {
    authenticatedFetch
      .get<ServerInstance>(`/server-instances/${id}`)
      .then(({ data }) => setServer(data))
      .catch(err => {
        console.error(err);
        toast(err.response?.data?.error || 'Failed to load server', 'error');
        navigate('/servers');
      });
  }

  // 1) Load the server
  useEffect(() => {
    if (!id) return;
    setLoading(true);
    loadServer();
    setLoading(false);
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
  if (!server.eulaAccepted && allowedToView('maintainer')) {
    tabs.push({
      label: 'Initialize',
      component: <InitializationPage serverInstance={server}
        isInitializing={isInitializing}
        setIsInitializing={setIsInitializing}
      />,
    });
  } else {
    // Always show Dashboard (or Overview)

    // If initialized:
    if (server.eulaAccepted) {
      // While roles are loading:
      if (!admin && rolesLoading) {
        tabs.push({
          label: 'Loading…',
          component: <p>Loading your permissions…</p>,
        });
      } else {

        // Viewer tabs
        if (allowedToView('viewer')) {
          tabs.push({
            label: 'Dashboard',
            component: <Dashboard server={server} />,
          });
          tabs.push({
            label: 'Console',
            component: (
              <Console
                isOperator={allowedToView('operator')}
                serverInstance={server}
                fetchServerInstance={loadServer} // To update state when starting/stopping
              />
            ),
          })
        }

        // Editor tabs
        if (allowedToView('editor')) tabs.push({
          label: "Propperties",
          component: <p>Placeholder</p>
        })
      }
    }
  }

  if (allowedToView('maintainer')) {
    tabs.push({
      label: 'Settings',
      disabled: isInitializing,
      component: (
        <ServerSettings
          server={server!}
          navigate={navigate}
          onUpdated={(updated: ServerInstance) => {
            // update local state so view reflects changes
            setServer(updated);
          }}
        />
      ),
    });
  }

  return (
    <main>
      <TabView
        tabs={tabs}
        title={server.name}
        subtitle={
          server.eulaAccepted ?
            <span className="server-status">
              <span
                className={`status-indicator__dot ${server.running ? 'status--running' : 'status--stopped'
                  }`}
              />
              {server.running ? 'Running' : 'Stopped'}
            </span> : "Uninitialized"
        }
      />
    </main>
  );
};

export default ServerInstanceView;
