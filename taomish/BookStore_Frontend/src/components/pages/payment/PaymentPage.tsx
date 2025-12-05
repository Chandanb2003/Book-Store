import { Link, Navigate } from "react-router-dom";
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { Quote } from "../../commons/quote/Quote"
import { useState } from "react";
import { useFetchPaymentFee } from "../../../utils/api_fetchers/payment_controller/useFetchPaymentFee";
import { LoadingSpinner } from "../../commons/loading_spinner/LoadingSpinner";
import { HttpErrorMessage } from "../../commons/http_error_message/HttpErrorMessage";
import { CardElement, useElements, useStripe } from "@stripe/react-stripe-js";
import { usePayFees } from "../../../utils/api_fetchers/payment_controller/usePayFees";
import { FormLoader } from "../../commons/form_loader/FormLoader";

export const PaymentPage = () => {

    const { authentication } = useAuthenticationContext();

    if (!authentication.isAuthenticated) return <Navigate to={"/"} />

    const [paymentFees, setPaymentFees] = useState(0);
    const [isLoadingPaymentFees, setIsLoadingPaymentFees] = useState(false);
    const [paymentFeesHttpError, setPaymentFeesHttpError] = useState<string | null>(null);
    const [submitPayFeesHttpError, setSubmitPayFeesHttpError] = useState<string | null>(null);
    const [payFeesBtnDisabled, setPayFeesBtnDisabled] = useState(false);

    useFetchPaymentFee(authentication, setPaymentFees, setIsLoadingPaymentFees, setPaymentFeesHttpError);

    const elements = useElements();
    const stripe = useStripe();

    const handlePayFeesClick = () => {

        usePayFees(authentication, elements, stripe, paymentFees, setPaymentFees, setPayFeesBtnDisabled, setSubmitPayFeesHttpError);
    }


    return (

        <div className="page-container">

            <Quote quoteId={4} />

            {isLoadingPaymentFees ? <LoadingSpinner /> : 
            
                <>

                    {paymentFeesHttpError ? <HttpErrorMessage httpError={paymentFeesHttpError} /> :
                        
                        <>

                            {paymentFees === 0 ? 

                                <div className="flex flex-col gap-10 items-center">

                                    <p className="text-2xl font-semibold">You have no outsanding fees yet.</p>

                                    <Link to={'/search'} className="custom-btn-2 text-center">
                                        Search for books
                                    </Link>

                                </div>

                                :

                                <div className="flex flex-col gap-10 px-5 w-full max-w-4xl">

                                    <div className="flex flex-col gap-1 items-center text-center border border-teal-800 rounded-md p-3 bg-teal-50 text-lg max-w-4xl">

                                        <p>This is a test page created to show the payment functionality. <span className="font-bold">Do not </span> 
                                        attempt to enter any valid credit card info. To test payment functionality enter card details provided below:</p>
                                        <p><span className="font-bold">Card number:</span> 4242 4242 4242 4242</p>
                                        <p><span className="font-bold">Expiration date:</span> any future date</p>
                                        <p><span className="font-bold">CVC:</span> any three digits</p>
                                        <p><span className="font-bold">Postal code:</span> any postal code</p>

                                    </div>

                                    <div className="flex flex-col gap-5 items-center w-full p-5 border border-teal-800 rounded-md shadow-custom-2">

                                        {submitPayFeesHttpError && <HttpErrorMessage httpError={submitPayFeesHttpError} />}

                                        <p className="text-xl font-semibold">Your pending fees: <span className="text-red-700">${paymentFees}</span></p>

                                        <FormLoader isLoading={payFeesBtnDisabled} />

                                        <p className="text-lg font-light">Pay with credit card:</p>

                                        <CardElement className="input" id="card-element" />

                                        <button className="custom-btn-2" disabled={payFeesBtnDisabled} onClick={handlePayFeesClick}>Pay Fees</button>

                                    </div>

                                </div>

                            }

                        </>

                    }

                </>

            }

        </div>

    )

}