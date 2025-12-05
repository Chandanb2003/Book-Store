import { useEffect } from "react";
import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useCheckIfBookCheckedOutByUser = (bookId: string,
                                               authentication: { isAuthenticated: boolean; token: string; },
                                               setIsCheckedOut: React.Dispatch<React.SetStateAction<boolean>>,
                                               setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                               setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

    useEffect(

        () => {

            const fetchIsBookCheckedOutByUser = async () => {

                if (authentication.isAuthenticated) {

                    const urlParams = `/${bookId}`;

                    const endpoint = book_controller_endpoints.is_checked_out;

                    const url = endpoint.url + urlParams;

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

                    setIsCheckedOut(responseJson);
                };

                setIsLoading(false);
            };

            fetchIsBookCheckedOutByUser().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, []

    );

}