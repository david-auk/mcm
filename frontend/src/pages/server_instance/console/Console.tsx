import React, { useState } from 'react';
import './Console.css';
import authenticatedFetch from '../../../utils/auth/authenticatedFetch';
import { useToast } from '../../../contexts/ToastContext';

interface ConsoleProps {
  /** whether to show the command input bar */
  isOperator: boolean;
  /** ID of the server instance */
  serverInstanceId: string;
}

const Console: React.FC<ConsoleProps> = ({ isOperator, serverInstanceId }) => {
  const [command, setCommand] = useState('');
  const toast = useToast();

  const handleAction = async (action: 'start' | 'stop' | 'restart') => {
    try {
      await authenticatedFetch.post<void>(`/server-instances/${serverInstanceId}/${action}`);
      toast(`Server ${action}ed successfully`, 'success');
    } catch (err: any) {
      toast(err.response?.data?.error || `Could not ${action} server`, 'error');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!command.trim()) return;
    // TODO: send `command` to backend
    console.log('Send command:', command);
    setCommand('');
  };

  return (
    <div className="console-container">
      {/* 1. Button array */}
      <div className="console-controls">
        {(['start','stop','restart'] as const).map(action => (
          <button
            key={action}
            className={`btn console-btn console-btn--${action}`}
            onClick={() => handleAction(action)}
          >
            {action.charAt(0).toUpperCase() + action.slice(1)}
          </button>
        ))}
      </div>

      {/* 2. Console screen placeholder */}
      <div className="console-screen">
        <p className="console-screen__placeholder">
          Server logs will appear here…
        </p>
      </div>

      {/* 3. Operator command input */}
      {isOperator && (
        <form className="console-input" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Enter command…"
            value={command}
            onChange={e => setCommand(e.target.value)}
          />
          <button type="submit" className="btn console-send-btn">
            Send
          </button>
        </form>
      )}
    </div>
  );
};

export default Console;
