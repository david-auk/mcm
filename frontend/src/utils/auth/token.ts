export function getToken(): string | null {const tokenString = localStorage.getItem('token');
  if (!tokenString) return null;

  try {
    const tokenData = JSON.parse(tokenString);
    if (Date.now() > tokenData.expiry) {
      // Token expired
      removeToken()
      return null;
    }
    return tokenData.value;
  } catch (e) {
    // Invalid JSON format
    removeToken()
    return null;
  }
}

export function setToken(token: string) {
  
  const expiry = Date.now() + 60 * 60 * 12000; // 12 hour from now (Same as backend JWT expiration)
  const tokenData = {
    value: token,
    expiry,
  };
  localStorage.setItem('token', JSON.stringify(tokenData));
}

export function removeToken() {
  localStorage.removeItem('token');
}

export function isAuthenticated(): boolean {
  return !!getToken();
}