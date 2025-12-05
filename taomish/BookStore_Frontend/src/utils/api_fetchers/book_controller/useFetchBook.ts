import { useEffect } from "react";
import { BookModel } from "../../../models/BookModel";
import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchBook = (bookId: string,
                             setBook: React.Dispatch<React.SetStateAction<BookModel>>,
                             setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                             setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

    useEffect(

        () => {

            const fetchBook = async () => {

                const urlParams = `/${bookId}`;

                const endpoint = book_controller_endpoints.find_by_id;
                
                const url = endpoint.url + urlParams;

                const response = await fetch(url);

                const responseJson = await response.json();

                if (!response.ok) {
                    throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                }

                const loadedBook: BookModel = responseJson;

                setBook(loadedBook);
                setIsLoading(false);
            }

            fetchBook().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, []

    );

}