import { useEffect } from "react";
import { payment_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchPaymentFee = (authentication: { isAuthenticated: boolean; token: string; },
                                   setPaymentFees: React.Dispatch<React.SetStateAction<number>>,
                                   setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                   setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

    useEffect(

        () => {

            const fetchPaymentFees = async () => {

                setIsLoading(true);

                if (authentication.isAuthenticated) {

                    const endpoint = payment_controller_endpoints.get_payment_info;

                    const url = endpoint.url;

                    const requestOptions = {

                        method: endpoint.method,
                        headers: {
                            Authorization: `Bearer ${authentication.token}`,
                            "Content-type": "application/json"
                        }
                    };

                    const response = await fetch(url, requestOptions);

                    const responseJson = await response.json();

                    if (!response.ok) {
                        throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                    }

                    setPaymentFees(responseJson);
                };

                setIsLoading(false);
            };

            fetchPaymentFees().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, []

    );

}