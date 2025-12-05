import { useEffect } from "react";
import { CheckoutModel } from "../../../models/CheckoutModel";
import { checkout_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchCurrentCheckouts = (authentication: { isAuthenticated: boolean; token: string; },
                                         setCurrentCheckouts: React.Dispatch<React.SetStateAction<CheckoutModel[]>>,
                                         setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                         setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                         isBookReturned: boolean,
                                         isCheckoutRenewed: boolean) => {

    useEffect(

        () => {

            const fetchUserCurrentCheckouts = async () => {

                setIsLoading(true);

                if (authentication.isAuthenticated) {

                    const endpoint = checkout_controller_endpoints.current_checkouts;

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

                    const loadedCheckouts: CheckoutModel[] = [];

                    for (const key in responseJson) {

                        loadedCheckouts.push(responseJson[key]);
                    }

                    setCurrentCheckouts(loadedCheckouts);
                    setIsLoading(false);
                };

                setIsLoading(false);
            };

            fetchUserCurrentCheckouts().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, [isBookReturned, isCheckoutRenewed]

    );

}