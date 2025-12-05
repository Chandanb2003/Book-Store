import { createContext, useContext } from "react";
import { LoginModel } from "../models/LoginModel";
import { RegistrationModel } from "../models/RegistrationModel";

type AuthenticationContextType = {

    authentication: { isAuthenticated: boolean, token: string, authority: string },

    register: (personDetails: RegistrationModel, 
               setIsLoading: React.Dispatch<React.SetStateAction<boolean>>, 
               setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => Promise<void>,

    login: (personDetails: LoginModel, 
            setIsLoading: React.Dispatch<React.SetStateAction<boolean>>, 
            setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => Promise<void>,
            
    logout: () => void
}

export const AuthenticationContext = createContext<AuthenticationContextType | undefined>(undefined);

export const useAuthenticationContext = () => {
    
    const authentication = useContext(AuthenticationContext);

    if (authentication === undefined) {
        throw new Error("useAuthenticationContext must be used within AuthenticationContext");
    }

    return authentication;
}