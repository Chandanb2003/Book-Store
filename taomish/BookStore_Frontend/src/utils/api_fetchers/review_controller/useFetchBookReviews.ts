import { useEffect } from "react";
import { ReviewModel } from "../../../models/ReviewModel";
import { review_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchBookReviews = (bookId: string,
                                    setReviews: React.Dispatch<React.SetStateAction<ReviewModel[]>>,
                                    setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                    setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                    setTotalAmountOfReviews: React.Dispatch<React.SetStateAction<number>>,
                                    urlPaginationParams: string,
                                    currentPage?: number,
                                    setTotlalPages?: React.Dispatch<React.SetStateAction<number>>) => {

    useEffect(

        () => {

            const fetchReviews = async () => {

                const urlParams = `/${bookId}` + urlPaginationParams;

                const endpoint = review_controller_endpoints.find_all_reviews;

                const url = endpoint.url + urlParams;

                const response = await fetch(url);

                const responseJson = await response.json();

                if (!response.ok) {
                    throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                }

                setTotalAmountOfReviews(responseJson.totalElements);
                if (setTotlalPages) setTotlalPages(responseJson.totalPages);

                const responseReviewsContentArray = responseJson.content;

                const loadedReviews: ReviewModel[] = [];

                for (const key in responseReviewsContentArray) {

                    loadedReviews.push(responseReviewsContentArray[key]);
                }

                setReviews(loadedReviews);
                setIsLoading(false);
            }

            fetchReviews().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, [currentPage]

    );

}