import { useEffect } from "react";
import { BookModel } from "../../../models/BookModel";
import { book_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchBooks = (currentPage: number,
                              setBooks: React.Dispatch<React.SetStateAction<BookModel[]>>,
                              setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                              setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                              setTotalAmountOfBooks?: React.Dispatch<React.SetStateAction<number>>,
                              setTotlalPages?: React.Dispatch<React.SetStateAction<number>>,
                              booksPerPage?: number,
                              urlSearchParams?: string,
                              isBookDeleted?: boolean) => {

    useEffect(

        () => {

            const fetchBooks = async () => {

                setIsLoading(true);

                const searchParams = urlSearchParams ? (urlSearchParams + "&") : "?";
                const paginationParams = `page=${currentPage - 1}&books-per-page=${booksPerPage ? booksPerPage : 9}`;
                const urlParams = searchParams + paginationParams;

                const find_all_endpoint = book_controller_endpoints.find_all_books;
                const search_by_title_endpoint = book_controller_endpoints.search_by_title;
                const search_by_genre_endpoint = book_controller_endpoints.search_by_genre;
                
                let url: string;

                if (searchParams.includes("genre-query")) {

                    url = search_by_genre_endpoint.url + urlParams;

                } else if (searchParams.includes("title-query")) {

                    url = search_by_title_endpoint.url + urlParams;
                    
                } else {

                    url = find_all_endpoint.url + urlParams;
                }

                const response = await fetch(url);

                const responseJson = await response.json();

                if (!response.ok) {
                    throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                }

                if (setTotalAmountOfBooks) setTotalAmountOfBooks(responseJson.totalElements);
                if (setTotlalPages) setTotlalPages(responseJson.totalPages);

                const responseBooksContentArray = responseJson.content;

                const loadedBooks: BookModel[] = [];

                for (const key in responseBooksContentArray) {

                    loadedBooks.push(responseBooksContentArray[key]);
                }

                setBooks(loadedBooks);
                setIsLoading(false);
            }

            fetchBooks().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, [currentPage, urlSearchParams, isBookDeleted]

    );

}