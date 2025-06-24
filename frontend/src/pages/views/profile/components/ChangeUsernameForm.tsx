// src/pages/views/profile/components/ChangeUsernameForm.tsx

import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import authenticatedFetch from '../../../../utils/auth/authenticatedFetch';
import {
    getUsername,
    setUsername as setLocalUsername,
} from '../../../../utils/auth/userDetails';
import { useToast } from '../../../../contexts/ToastContext';
import './ChangeUsernameForm.css';

const ChangeUsernameForm: React.FC = () => {
    const currentUsername = getUsername();
    const [newUsername, setNewUsername] = useState(currentUsername);
    const [checking, setChecking] = useState(false);
    const [available, setAvailable] = useState<boolean | null>(null);

    // For aborting in-flight checks
    const aborter = useRef<AbortController | null>(null);
    // For debouncing input
    const debounceRef = useRef<number | null>(null);

    const toast = useToast();

    // Debounce and trigger availability check on every newUsername change
    useEffect(() => {
        if (!newUsername) return;

        // Clear any existing debounce timer
        if (debounceRef.current !== null) {
            clearTimeout(debounceRef.current);
        }
        const trimmed = newUsername.trim();

        // nothing or same as before → clear
        if (!trimmed || trimmed === currentUsername) {
            setAvailable(null);
            return;
        }

        // wait 300ms after last keystroke
        debounceRef.current = window.setTimeout(() => {
            checkAvailability(trimmed);
        }, 300);

        // cleanup on unmount or on next change
        return () => {
            if (debounceRef.current !== null) {
                clearTimeout(debounceRef.current);
            }
            if (aborter.current) {
                aborter.current.abort();
            }
        };
    }, [newUsername]);

    // Check username availability
    const checkAvailability = async (name: string) => {
        if (aborter.current) aborter.current.abort();
        const ctl = new AbortController();
        aborter.current = ctl;

        setChecking(true);
        try {
            const { data } = await authenticatedFetch.get<{ in_use: boolean }>(
                `/users/username-in-use/${encodeURIComponent(name)}`,
                { signal: ctl.signal }
            );
            setAvailable(!data.in_use);
        } catch (err: any) {
            if (!axios.isCancel(err)) {
                toast('Username check failed', 'error');
                setAvailable(null);
            }
        } finally {
            setChecking(false);
        }
    };

    // Submit new username
    const submit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!available) return;
        if (!newUsername) return;

        const trimmed = newUsername.trim();
        if (!trimmed) return;

        try {
            await authenticatedFetch.post('/user/me/change-username', {
                new_username: trimmed,
            });
            setLocalUsername(trimmed);
            // let HomeScreen know to refresh
            window.dispatchEvent(new Event('username-changed'));
            toast('Username Updated', 'success');
        } catch (err: any) {
            toast(err.response?.data?.error || 'Could not update username', 'error');
        }
    };

    return (
        <form className="profile-form" onSubmit={submit}>
            <h3>Personal Info</h3>

            <label htmlFor="new-username">New Username</label>
            <input
                id="new-username"
                type="text"
                value={newUsername || ""}
                onChange={(e) => {
                    setNewUsername(e.target.value);
                    setAvailable(null);
                }}
                required
            />

            {checking && <p className="availability">Checking…</p>}

            {available === true && (
                <p className="availability success">
                    <span className="availability-icon">✓</span>
                    Available
                </p>
            )}
            {available === false && (
                <p className="availability error">
                    <span className="availability-icon">✖</span>
                    Taken
                </p>
            )}

            <button
                type="submit"
                disabled={
                    !available ||
                    !newUsername ||
                    newUsername.trim() === '' ||
                    newUsername.trim() === currentUsername
                }
            >
                Change Username
            </button>
        </form>
    );
};

export default ChangeUsernameForm;
