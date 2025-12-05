import { useState } from "react"
import { Quote } from "../../commons/quote/Quote"
import { RegistrationModel } from "../../../models/RegistrationModel";
import { FieldErrors } from "../../commons/field_errors/FieldErrors";
import { FormLoader } from "../../commons/form_loader/FormLoader";
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { Link } from "react-router-dom";
import { HttpErrorMessage } from "../../commons/http_error_message/HttpErrorMessage";

export const RegistrationPage = () => {

    const { authentication, register } = useAuthenticationContext();

    const [personDetails, setPersonDetails] = useState<RegistrationModel>({firstName: "", lastName: "", dateOfBirth: new Date("1800-01-01"), email: "", password: ""});
    const [isLoading, setIsLoading] = useState(false);
    const [httpError, setHttpError] = useState<string | null>(null);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {

        setPersonDetails({ ...personDetails, [event.target.name]: event.target.value });
    };

    const handleRegisterClick = async () => {

        await register(personDetails, setIsLoading, setHttpError);
    };

    return (

        <div className="page-container px-5">

            <Quote quoteId={8} />
            
            <div className="custom-form">

                <p className="text-center text-3xl font-semibold">Register</p>

                <FormLoader isLoading={isLoading} />

                {(httpError && !httpError.startsWith("Some")) && <HttpErrorMessage httpError={httpError} />}

                {!authentication.isAuthenticated ? 

                    <>

                        <form className="flex flex-col gap-5 w-full">

                            <div className="flex flex-col gap-1">

                                <FieldErrors fieldName="firstName" httpError={httpError} />
                                <input type="text" name="firstName" onChange={handleChange} placeholder="First name" className="input shadow-md"/>
                            
                            </div>

                            <div className="flex flex-col gap-1">

                                <FieldErrors fieldName="lastName" httpError={httpError} />
                                <input type="text" name="lastName" onChange={handleChange} placeholder="Last name" className="input shadow-md"/>

                            </div>
                            
                            <div className="flex flex-col gap-1">

                                <FieldErrors fieldName="dateOfBirth" httpError={httpError} />

                                <div className="flex gap-5 items-center whitespace-nowrap pl-1">

                                    Date of birth :

                                    <input type="date" name="dateOfBirth" onChange={handleChange} className="input shadow-md"/>
                                
                                </div>

                            </div>

                            <div className="flex flex-col gap-1">

                                <FieldErrors fieldName="email" httpError={httpError} />
                                <input type="text" name="email" onChange={handleChange} placeholder="E-mail" className="input shadow-md"/>
                            
                            </div>

                            <div className="flex flex-col gap-1">

                                <FieldErrors fieldName="password" httpError={httpError} />
                                <input type="password" name="password" onChange={handleChange} placeholder="Password" className="input shadow-md"/>
                            
                            </div>

                        </form>

                        <button className="custom-btn-2" onClick={handleRegisterClick}>Register</button>

                    </>

                    :

                    <>

                        <div className="flex flex-col gap-5 text-center text-lg">

                            <p>Your new account is created successfully!</p>
                            <p>Thank you for joining our community.</p>
                            <p>You are now logged in and may brouse through all the sections of our store.</p>

                        </div>

                        <Link to={"/home"} className="custom-btn-2">Back to home</Link>

                    </>

                }

            </div>
            
        </div>

    )

}