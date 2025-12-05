import { ReactNode, useState } from "react";
import { LoginModel } from "../models/LoginModel";
import { useLogin } from "../utils/api_fetchers/authentication_controller/useLogin";
import { AuthenticationContext } from "./authenticationContext";
import { useAuthenticationState } from "./useAuthenticationState";
import { RegistrationModel } from "../models/RegistrationModel";
import { useRegister } from "../utils/api_fetchers/authentication_controller/useRegister";

type AuthenticationProviderProps = {
    children: ReactNode
}

export const AuthenticationProvider = ({ children }: AuthenticationProviderProps) => {

    const authenticationState = useAuthenticationState();

    const [authentication, setAuthentication] = useState(authenticationState);

    const register = async (personDetails: RegistrationModel,
                            setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                            setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

        await useRegister(personDetails, setIsLoading, setHttpError, setAuthentication);
    };

    const login = async (personDetails: LoginModel, 
                         setIsLoading: React.Dispatch<React.SetStateAction<boolean>>, 
                         setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

        await useLogin(personDetails, setIsLoading, setHttpError, setAuthentication);
    }

    const logout = () => {

        setAuthentication({ isAuthenticated: false, token: "", authority: "" });
        localStorage.setItem("authenticationState", JSON.stringify({ isAuthenticated: false, token: "", authority: "" }));
    }

    return (

        <AuthenticationContext.Provider value={{ authentication, register, login, logout }}>

            {children}

        </AuthenticationContext.Provider>

    );

}