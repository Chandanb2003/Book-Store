import { admin_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useDeleteBook= async (bookId: string,
                                   authentication: { isAuthenticated: boolean; token: string; },
                                   setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                   setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                   setIsBookDeleted: React.Dispatch<React.SetStateAction<boolean>>) => {

    const deleteBook = async () => {

        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const urlParams = `/${bookId}`;

            const endpoint = admin_controller_endpoints.delete_book;

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

            setIsBookDeleted(prev => !prev);
        }

        setHttpError(null);
        setIsLoading(false);
    }

    deleteBook().catch(
        
        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )
};