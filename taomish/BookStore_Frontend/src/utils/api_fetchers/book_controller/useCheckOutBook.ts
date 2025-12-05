import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useCheckOutBook = async (bookId: string,
                                      authentication: { isAuthenticated: boolean; token: string; },
                                      setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                      setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                      setCopiesAvailable: React.Dispatch<React.SetStateAction<number>>,
                                      setIsCheckedOut: React.Dispatch<React.SetStateAction<boolean>>,
                                      setCurrentCheckoutsCount: React.Dispatch<React.SetStateAction<number>>) => {

    const checkOutBook = async () => {
        
        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const urlParams = `/${bookId}`;

            const endpoint = book_controller_endpoints.checkout_book;

            const url = endpoint.url + urlParams;

            const requestOptions = {

                method: endpoint.method,
                headers: {
                    Authorization: `Bearer ${authentication.token}`,
                    "Content-type": "application/json"
                }
            };

            const response = await fetch(url, requestOptions);

            if (!response.ok) {
                
                const responseJson = await response.json();
                throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
            }

            setCopiesAvailable(prev => prev - 1);
            setIsCheckedOut(true);
            setCurrentCheckoutsCount(prev => prev + 1);
        }
        
        setIsLoading(false);
    }

    checkOutBook().catch(

        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )

};