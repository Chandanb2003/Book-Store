import { useEffect } from "react";
import { checkout_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchCurrentCheckoutsCount = (authentication: { isAuthenticated: boolean; token: string; },
                                              setCurrentCheckoutsCount: React.Dispatch<React.SetStateAction<number>>,
                                              setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                              setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

    useEffect(

        () => {

            const fetchUserCurrentCheckoutsCount = async () => {

                if (authentication.isAuthenticated) {

                    const endpoint = checkout_controller_endpoints.current_loans_count;

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

                    setCurrentCheckoutsCount(responseJson);
                };

                setIsLoading(false);
            };

            fetchUserCurrentCheckoutsCount().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, []

    );

}