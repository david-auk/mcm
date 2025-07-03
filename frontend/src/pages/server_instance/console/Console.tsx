import React, { useState, useEffect, useRef } from 'react';
import './Console.css';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import type ServerInstance from '../ServerInstance';

interface ConsoleProps {
  /** whether to show the command input bar */
  isOperator: boolean;
  serverInstance: ServerInstance;
  fetchServerInstance: () => void;
}

const Console: React.FC<ConsoleProps> = ({ isOperator, serverInstance, fetchServerInstance }) => {
  const [command, setCommand] = useState('');
  const [logs, setLogs] = useState<string[]>([]);
  const logConsoleRef = useRef<HTMLDivElement>(null);
  const [isFollowing, setIsFollowing] = useState(true);
  const toast = useToast();

  const handleAction = async (action: 'start' | 'stop' | 'restart') => {
    try {
      await authenticatedFetch.post<void>(`/server-instances/${serverInstance.id}/${action}`);
      if (action === 'start') {
        setLogs([]);
      }
      toast(`Server ${action}ed successfully`, 'success');

      // Refetch the current state
      fetchServerInstance();
    } catch (err: any) {
      toast(err.response?.data?.error || `Could not ${action} server`, 'error');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!command.trim()) return;
    try {
      const { data } = await authenticatedFetch.post<{ output: string }>(
        `/server-instances/${serverInstance.id}/command`,
        { command }
      );
      // Append the sent command and its output to the log console
      setLogs(prevLogs => [...prevLogs, `> ${command}`, data.output]);
      toast('Command sent successfully', 'success');
    } catch (err: any) {
      toast(err.response?.data?.error || 'Failed to send command', 'error');
    } finally {
      setCommand('');
    }
  };

  const handleScroll = () => {
    const div = logConsoleRef.current;
    if (!div) return;
    const { scrollTop, scrollHeight, clientHeight } = div;
    const atBottom = scrollTop + clientHeight >= scrollHeight - 100;
    setIsFollowing(atBottom);
  };

useEffect(() => {
  let intervalId: number;
  const fetchLogs = async () => {
    try {
      const { data } = await authenticatedFetch.get<string[]>(
        `/server-instances/${serverInstance.id}/log?fromHead=${logs.length}`
      );
      if (data) {
        setLogs(prevLogs => [...prevLogs, ...data]);
      }
    } catch (err) {
      toast('Could not fetch logs', 'error');
    }
  };

  if (serverInstance.running) {
    // Active server: initial fetch and polling
    fetchLogs();
    intervalId = window.setInterval(fetchLogs, 500);
  } else if (logs.length === 0) {
    // Server down on initial load: fetch entire log once
    fetchLogs();
  }

  return () => {
    if (intervalId) {
      clearInterval(intervalId);
    }
  };
}, [serverInstance.id, serverInstance.running, logs.length]);

  useEffect(() => {
    const div = logConsoleRef.current;
    if (!div || !isFollowing) return;
    div.scrollTop = div.scrollHeight;
  }, [logs, isFollowing]);

  return (
    <div className="console-container">
      {/* 1. Button controls based on server state */}
      <div className="console-controls">
        {!serverInstance.running && (
          <button
            className="success"
            onClick={() => handleAction('start')}
          >
            Start
          </button>
        )}
        {serverInstance.running && (
          <>
            <button
              className="danger"
              onClick={() => handleAction('stop')}
            >
              Stop
            </button>
            <button
              className="warning"
              onClick={() => handleAction('restart')}
            >
              Restart
            </button>
          </>
        )}
      </div>

      <div className="log-console" ref={logConsoleRef} onScroll={handleScroll}>
        {logs.map((entry, i) => (
          <div key={i} className="log-entry">
            {entry}
          </div>
        ))}
      </div>

      {/* 3. Operator command input */}
      {isOperator && (
        <form className="console-input" onSubmit={handleSubmit}>
          <input
            disabled={!serverInstance.running}
            type="text"
            placeholder="Enter command…"
            value={command}
            onChange={e => setCommand(e.target.value)}
          />
          <button type="submit" className="success"
            disabled={!serverInstance.running}>
            Send
          </button>
        </form>
      )}
    </div>
  );
};

export default Console;
