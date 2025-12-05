import { ReviewModel } from "../../../models/ReviewModel";
import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useSubmitReview = async (bookId: string,
                                      authentication: { isAuthenticated: boolean; token: string; },
                                      review: ReviewModel,
                                      setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                      setIsReviewLeft: React.Dispatch<React.SetStateAction<boolean>>,
                                      setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                      setIsRatingChanged: React.Dispatch<React.SetStateAction<boolean>>) => {

    const submitReview = async () => {

        setIsLoading(true);

        if (authentication.isAuthenticated) {

            const urlParams = `/${bookId}`;

            const endpoint = book_controller_endpoints.review_book;

            const url = endpoint.url + urlParams;
            
            const requestOptions = {

                method: endpoint.method,
                headers: {
                    Authorization: `Bearer ${authentication.token}`,
                    "Content-type": "application/json"
                },
                body: JSON.stringify(review)
            };

            const response = await fetch(url, requestOptions);

            const responseJson = await response.json();

            if (!response.ok) {
                throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
            }
            
            setIsReviewLeft(true);
            setIsRatingChanged(prev => !prev);
        }

        setIsLoading(false);
    }

    submitReview().catch(
        
        (error: any) => {

            setIsLoading(false);
            setHttpError(error.message);
        }
    )

};