import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setToken } from '../utils/auth/token';
import { setUserIsAdmin, setUsername as setLocalUsername } from '../utils/auth/userDetails';
import { useToast } from '../contexts/ToastContext'


import './LoginScreen.css'; // Import the CSS file for styling

const LoginScreen: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const toast = useToast()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const res = await fetch('/api/auth/login', {
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username, password
                })
            });

            const data = await res.json();

            if (!res.ok) {

                toast(`Login failed – ${data.error}`, 'error')
                return
            }


            const token = data.token;

            // Store values
            setToken(token);
            setUserIsAdmin(data.is_admin)
            setLocalUsername(username)

            // Display message
            toast('Welcome back!', 'success')

            navigate('/home');
        } catch (err: any) {
            console.error(err);
        }
    };

    return (
        <main className="main-centered">
            <div className="container-login">
                <form onSubmit={handleSubmit}>
                    <h2>Login</h2>
                    <label htmlFor="username">Username:</label>
                    <input
                        id="username"
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        required
                    />
                    <label htmlFor="password">Password:</label>
                    <input
                        id="password"
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit">Login</button>
                </form>
            </div>
        </main>
    );
};

export default LoginScreen;
