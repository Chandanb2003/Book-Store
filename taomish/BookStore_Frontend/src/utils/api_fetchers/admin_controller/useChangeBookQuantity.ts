import { admin_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useChangeBookQuantity= async (bookId: string,
                                           operation: "increase" | "decrease",
                                           authentication: { isAuthenticated: boolean; token: string; },
                                           setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                           setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                           setTotalQuantity: React.Dispatch<React.SetStateAction<number>>,
                                           setAvailableQuantity: React.Dispatch<React.SetStateAction<number>>) => {

    const changeBookQuantity = async () => {

        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const urlParams = `/${bookId}`;

            const increase_endpoint = admin_controller_endpoints.increase_quantity;
            const decrease_endpoint = admin_controller_endpoints.decrease_quantity;

            const url = (operation === "increase" ? increase_endpoint.url : decrease_endpoint.url) + urlParams;
            
            const requestOptions = {

                method: operation === "increase" ? increase_endpoint.method : decrease_endpoint.method,
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
            
            if (operation === "increase") {

                setTotalQuantity(prev => prev + 1);
                setAvailableQuantity(prev => prev + 1);

            } else if (operation === "decrease") {
                
                setTotalQuantity(prev => prev - 1);
                setAvailableQuantity(prev => prev - 1);
            }

        }

        setHttpError(null);
        setIsLoading(false);
    }

    changeBookQuantity().catch(
        
        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )
};