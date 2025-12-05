import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useReturnBook = async (bookId: string,
                                    authentication: { isAuthenticated: boolean; token: string; },
                                    setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                    setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                    setIsBookReturned: React.Dispatch<React.SetStateAction<boolean>>) => {

    const returnBook = async () => {

        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const urlParams = `/${bookId}`;

            const endpoint = book_controller_endpoints.return_book;

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
            
            setIsBookReturned(prev => !prev);
        }

        setIsLoading(false);
    }

    returnBook().catch(
        
        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )

};