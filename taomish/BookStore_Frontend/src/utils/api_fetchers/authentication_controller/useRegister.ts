import jwtDecode from "jwt-decode";
import { RegistrationModel } from "../../../models/RegistrationModel";
import { authentication_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useRegister = async (registrationModel: RegistrationModel, 
                                  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>, 
                                  setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                  setAuthentication: React.Dispatch<React.SetStateAction<{ isAuthenticated: boolean; token: string; authority: string }>>) => {

    const submitRegister = async () => {

        setIsLoading(true);

        const endpoint = authentication_controller_endpoints.register_user;

        const url = endpoint.url;

        const requestOptions = {

            method: endpoint.method,
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify(registrationModel)
        };

        const response = await fetch(url, requestOptions);

        const responseJson = await response.json();

        if (!response.ok) {
            throw new Error(responseJson.message);
        }

        const token = responseJson.token;

        const payload: {role: {authority: string}[], sub: string, iss: string, iat: number, exp: number} = jwtDecode(token);
        
        setAuthentication({ isAuthenticated: true, token: token, authority: payload.role[0].authority });

        localStorage.setItem("authenticationState", JSON.stringify({ isAuthenticated: true, token: token, authority: payload.role[0].authority }));

        setIsLoading(false);
    }

    submitRegister().catch(

        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )

};