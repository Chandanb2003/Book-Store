import { DiscussionModel } from "../../../models/DiscussionModel";
import { admin_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useSubmitDiscussionResponse = async (authentication: { isAuthenticated: boolean; token: string; },
                                                  discussionModel: DiscussionModel,
                                                  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                                  setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                                  setIsDiscussionClosed: React.Dispatch<React.SetStateAction<boolean>>) => {

    const submitDiscussionResponse = async () => {

        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const endpoint = admin_controller_endpoints.close_discussion;

            const url = endpoint.url;
            
            const requestOptions = {

                method: endpoint.method,
                headers: {
                    Authorization: `Bearer ${authentication.token}`,
                    "Content-type": "application/json"
                },
                body: JSON.stringify(discussionModel)
            };

            const response = await fetch(url, requestOptions);

            if (!response.ok) {

                const responseJson = await response.json();
                throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
            }

            setIsDiscussionClosed(prev => !prev);
        }

        setHttpError(null);
        setIsLoading(false);
    }

    submitDiscussionResponse().catch(
        
        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )
};