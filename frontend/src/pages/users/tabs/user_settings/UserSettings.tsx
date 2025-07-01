import EditPersonForm from './components/EditPersonForm';
import DeleteUserButton from './components/DeleteUserButton';
import ToggleAdminButton from './components/ToggleAdminButton';
import type { UserResponse } from '../../UserView';
import { useState } from 'react';
import type { NavigateFunction } from 'react-router-dom';


interface UserSettingsProps {
  user: UserResponse["user"];
  isAdmin: boolean;
  isSelf: boolean;
  fetchUser: () => Promise<void>;
  navigate: NavigateFunction;
}

const UserSettings: React.FC<UserSettingsProps> = ({
  user,
  isAdmin,
  isSelf,
  fetchUser,
  navigate,
}) => {
  const [submitting, setSubmitting] = useState(false);

  return (
    <section className="user-settings">
      <div className="user-actions">
        <ToggleAdminButton
          userId={user.id}
          isAdmin={isAdmin}
          isSelf={isSelf}
          onToggled={fetchUser}
          disabled={submitting}
        />
        <DeleteUserButton
          userId={user.id}
          disabled={submitting}
          isSelf={isSelf}
          onDeleted={() => navigate('/home')}
        />
      </div>
      <EditPersonForm
        initialUsername={user.username}
        userId={user.id}
        onSaved={fetchUser}
        submitting={submitting}
        setSubmitting={setSubmitting}
        isSelf={isSelf}
      />
    </section>
  )
}

export default UserSettings;