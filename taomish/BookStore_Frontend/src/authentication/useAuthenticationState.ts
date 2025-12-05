export const useAuthenticationState = () => {

    const authenticationStateItem = localStorage.getItem("authenticationState");
    if (authenticationStateItem) return(JSON.parse(authenticationStateItem));

    return { isAuthenticated: false, token: "", authority: "" };
}