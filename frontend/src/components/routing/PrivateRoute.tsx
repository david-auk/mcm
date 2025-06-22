import type { ReactNode, ReactElement } from 'react';
import { Navigate } from 'react-router-dom';
import { isAuthenticated } from '../../utils/auth/token';

interface Props {
    children: ReactNode;
}

const PrivateRoute = ({ children }: Props): ReactElement | null => {
    return isAuthenticated() ? <>{children}</> : <Navigate to="/login" />;
};

export default PrivateRoute;