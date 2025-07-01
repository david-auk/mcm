export function setUserId(id: string) {
    localStorage.setItem("user_id", id)
}

export function getUserId() {   
    return localStorage.getItem("user_id")
}

export function setUsername(username: string) {
    localStorage.setItem("username", username)
}

export function getUsername() {
    return localStorage.getItem("username")
}

export function setUserIsAdmin(value: boolean) {
    localStorage.setItem('is_admin', value.toString())
}

export function isAdmin() {
    const localVal = localStorage.getItem('is_admin')

    if (localVal == null) {
        return false
    }

    return localVal.toLowerCase() === 'true'
}

export function clearUserDetails() {
    localStorage.removeItem("is_admin")
    localStorage.removeItem("username")
}