import React, { useState, useEffect, useRef } from 'react';
import './Console.css';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';
import { AnsiUp } from 'ansi_up';
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
  const logsRef = useRef<string[]>([]);
  const ansiUp = new AnsiUp();

  useEffect(() => {
    logsRef.current = logs;
  }, [logs]);
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
    let intervalId: number | undefined;

    const fetchInitialLogs = async () => {
      try {
        const { data } = await authenticatedFetch.get<string[]>(
          `/server-instances/${serverInstance.id}/log`
        );
        setLogs(data);
      } catch (err) {
        toast('Could not fetch logs', 'error');
      }
    };

    const fetchNewLogs = async () => {
      try {
        const { data } = await authenticatedFetch.get<string[]>(
          `/server-instances/${serverInstance.id}/log?fromHead=${logsRef.current.length}`
        );
        if (data.length) {
          setLogs(prevLogs => [...prevLogs, ...data]);
        }
      } catch (err) {
        toast('Could not fetch logs', 'error');
      }
    };

    // Always fetch the full log on mount or when serverInstance changes
    fetchInitialLogs();

    // If server is running, start polling for new entries
    if (serverInstance.running) {
      intervalId = window.setInterval(fetchNewLogs, 500);
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [serverInstance.id, serverInstance.running]);

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
          <div
            key={i}
            className="log-entry"
            // transform ANSI codes to HTML and inject
            dangerouslySetInnerHTML={{
              __html: ansiUp.ansi_to_html(entry)
            }}
          />
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
