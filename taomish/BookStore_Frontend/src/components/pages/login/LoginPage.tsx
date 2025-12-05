import { Link, Navigate } from "react-router-dom"
import { Quote } from "../../commons/quote/Quote"
import { FieldErrors } from "../../commons/field_errors/FieldErrors"
import { useState } from "react";
import { LoginModel } from "../../../models/LoginModel";
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { FormLoader } from "../../commons/form_loader/FormLoader";
import { HttpErrorMessage } from "../../commons/http_error_message/HttpErrorMessage";

export const LoginPage = () => {

    const { authentication, login } = useAuthenticationContext();

    const [personDetails, setPersonDetails] = useState<LoginModel>({ email: "", password: "" });
    const [isLoading, setIsLoading] = useState(false);
    const [httpError, setHttpError] = useState<string | null>(null);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {

        setPersonDetails({ ...personDetails, [event.target.name]: event.target.value });
    };

    const handleSignInClick = async () => {

        await login(personDetails, setIsLoading, setHttpError);
    };
    
    if (authentication.isAuthenticated) return <Navigate to={"/"} />

    return (

        <div className="page-container px-5">

            <Quote quoteId={9} />
            
            <div className="custom-form">

                <p className="text-center text-3xl font-semibold">Sign In</p>

                <FormLoader isLoading={isLoading} />

                {(httpError && !httpError.startsWith("Some")) && <HttpErrorMessage httpError={httpError} />}

                <form className="flex flex-col gap-5 w-full">

                    <div className="flex flex-col gap-1">

                        <FieldErrors fieldName="email" httpError={httpError} />
                        <input type="text" name="email" onChange={handleChange} placeholder="E-mail" className="input shadow-md"/>

                    </div>

                    <div className="flex flex-col gap-1">

                        <FieldErrors fieldName="password" httpError={httpError} />
                        <input type="password" name="password" onChange={handleChange} placeholder="Password" className="input shadow-md"/>

                    </div>

                </form>

                <div className="w-full flex flex-col gap-3 justify-between text-center">
                    
                    <button className="custom-btn-2" onClick={handleSignInClick}>Sign In</button>

                    Or create new account :

                    <Link to={"/register"} className="custom-btn-2">Register</Link>
                
                </div>

            </div>
            
        </div>

    )

}