import React from 'react';
import type ServerInstance from '../ServerInstance';
import './Dashboard.css';

interface DashboardProps {
  server: ServerInstance;
}

const Dashboard: React.FC<DashboardProps> = ({ server }) => {
  return (
    <div className="dashboard-container">
      {/* Status card */}
      <div className="dashboard-card">
        <div className="dashboard-card__title">Status</div>
        <div className="dashboard-card__value">
          <span
            className={`status-indicator__dot ${
              server.running ? 'status--running' : 'status--stopped'
            }`}
          />
          {server.running ? 'Running' : 'Stopped'}
        </div>
      </div>

      {/* Server Info cards */}
      <div className="dashboard-card">
        <div className="dashboard-card__title">Version</div>
        <div className="dashboard-card__value">{server.minecraftVersion}</div>
      </div>

      <div className="dashboard-card">
        <div className="dashboard-card__title">RAM Allocated</div>
        <div className="dashboard-card__value">{server.allocatedRamMB} MB</div>
      </div>

      <div className="dashboard-card">
        <div className="dashboard-card__title">Port</div>
        <div className="dashboard-card__value">{server.port}</div>
      </div>

      <div className="dashboard-card dashboard-card--wide">
        <div className="dashboard-card__title">Description</div>
        <div className="dashboard-card__value">
          {server.description || '—'}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
