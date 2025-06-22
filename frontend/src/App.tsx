// src/App.tsx
import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import PrivateRoute from './components/routing/PrivateRoute';
import LoginScreen from './pages/LoginScreen';
import HomeScreen from './pages/HomeScreen';

import { ToastProvider } from './contexts/ToastContext';
import './App.css';

const backgrounds = [
  '/backgrounds/dirt_bg.png',
  '/backgrounds/nether_bg.png',
  '/backgrounds/portal_bg.png',
];

const App: React.FC = () => {
  useEffect(() => {
    const pick = backgrounds[Math.floor(Math.random() * backgrounds.length)];
    document.body.style.background = `url('${pick}') center center fixed`;
    document.body.style.backgroundSize = 'cover';
    document.body.style.imageRendering = 'pixelated';
    document.documentElement.style.setProperty('--bg-image', `url('${pick}')`);
  }, []);

  return (
    <ToastProvider> {/* ← wrap the entire app in toast so messages can be displayed at any time */}
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginScreen />} />
          <Route
            path="/home"
            element={
              <PrivateRoute>
                <HomeScreen />
              </PrivateRoute>
            }
          />
          <Route path="*" element={<LoginScreen />} />
        </Routes>
      </BrowserRouter>
    </ToastProvider>
  );
};

export default App;
