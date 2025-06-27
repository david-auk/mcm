import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './InitializationPage.css';
import { useToast } from '../../../contexts/ToastContext';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import type ServerInstance from '../ServerInstance';

interface LogEntry {
  timestamp: string;
  message: string;
}

interface ProcessStatus {
  state: 'RUNNING' | 'SUCCESS' | 'ERROR';
  logs: LogEntry[];
}

interface InitializationPageProps {
  serverInstance: ServerInstance;
}

export default function InitializationPage({ serverInstance }: InitializationPageProps) {
  const navigate = useNavigate();
  const toast = useToast();

  const [processId, setProcessId] = useState<string | null>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [state, setState] = useState<ProcessStatus['state']>('RUNNING');
  const [initLoading, setInitLoading] = useState(false);

  useEffect(() => {
    if (!processId) return;
    const interval = setInterval(async () => {
      try {
        const { data: status } = await authenticatedFetch.get<ProcessStatus>(
          `/server-instances/initialize/status/${processId}`
        );
        setLogs(status.logs);
        setState(status.state);
        if (status.state === 'ERROR') {
          toast('Initialization failed. Check logs for details.', 'error');
        }
        if (status.state !== 'RUNNING') {
          clearInterval(interval);
          if (status.state === 'SUCCESS') {
            toast('Initialization complete!', 'success');
          }
        }
      } catch (error: any) {
        clearInterval(interval);
        toast(error.message || 'Error polling status', 'error');
      }
    }, 1000);
    return () => clearInterval(interval);
  }, [processId, toast]);

  const startInit = async () => {
    setInitLoading(true);
    try {
      const response = await authenticatedFetch.post<{ processId: string }>(
        `/server-instances/initialize/${serverInstance.id}`
      );
      const { processId: pid } = response.data;
      setProcessId(pid);
      toast('Initialization started.', 'info');
    } catch (error: any) {
      toast(error.response?.data?.error || 'Failed to start initialization', 'error');
    } finally {
      setInitLoading(false);
    }
  };

  const reset = () => {
    setProcessId(null);
    setLogs([]);
    setState('RUNNING');
  };

  return (
    <div className="init-container">
      <div className="init-card">
        <h2 className="init-header">Server Initialization</h2>

        {!processId && (
          <button
            className="init-button"
            onClick={startInit}
            disabled={initLoading}
          >
            {initLoading ? 'Starting...' : 'Start Initialization'}
          </button>
        )}

        {processId && (
          <>
            <p className="init-subheader">Initializing your server… you can monitor progress below.</p>
            <div className="log-console">
              {logs.map((entry, i) => (
                <div key={i} className="log-entry">
                  [{new Date(entry.timestamp).toLocaleTimeString()}] {entry.message}
                </div>
              ))}
            </div>
            {state === 'SUCCESS' && (
              <div className="success-message">
                <span>Initialization complete.</span>
                <span>Reload to View Server</span>
              </div>
            )}
            {state === 'ERROR' && (
              <button
                className="init-button outline"
                onClick={reset}
              >
                Retry
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}