import { useEffect } from "react";
import { review_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchBookAverageRating = (bookId: string,
                                          setAverageRating: React.Dispatch<React.SetStateAction<number>>,
                                          setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                          setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                          isRatingChanged: boolean) => {

    useEffect(

        () => {

            const fetchAverageRating = async () => {

                const urlParams = `/${bookId}`;

                const endpoint = review_controller_endpoints.get_avg_rating;
                
                const url = endpoint.url + urlParams;

                const response = await fetch(url);

                const responseJson = await response.json();

                if (!response.ok) {
                    throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                }

                const roundedRating = (Math.round((responseJson) * 2) / 2).toFixed(1);

                setAverageRating(Number(roundedRating));

                setIsLoading(false);
            }

            fetchAverageRating().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, [isRatingChanged]

    );

}