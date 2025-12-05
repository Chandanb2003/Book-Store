import { useState } from "react";
import { BookModel } from "../../../../models/BookModel";
import { useFetchBooks } from "../../../../utils/api_fetchers/book_controller/useFetchBooks";
import { Pagination } from "../../../commons/pagination/Pagination";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { QuantitiesTabBookCard } from "./QuantitiesTabBookCard";
import { PaginatedItemsCount } from "../../../commons/pagination/PaginatedItemsCount";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

export const QuantitiesTab = () => {

    const [books, setBooks] = useState<BookModel[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [httpError, setHttpError] = useState<string | null>(null);
    const [isBookDeleted, setIsBookDeleted] = useState(false);

    const [currentPage, setCurrentPage] = useState(1);
    const [totalAmountOfBooks, setTotalAmountOfBooks] = useState(0);
    const [totalPages, setTotlalPages] = useState(0);
    const [resultRange, setResultRange] = useState({start: 1, end: 5});

    const [titleQuery, setTitleQuery] = useState("");
    const [searchParams, setSearchParams] = useState("");
    
    const handleSearchClick = () => {

        setHttpError(null);
        setSearchParams(`?title-query=${titleQuery}`);
    };

    useFetchBooks(currentPage, setBooks, setIsLoading, setHttpError, setTotalAmountOfBooks, setTotlalPages, 5, searchParams, isBookDeleted);

    return (

        <div className="flex flex-col gap-10">

            <div className="flex gap-5">

                <input className="input" placeholder="Search books by title..." value={titleQuery} onChange={event => setTitleQuery(event.target.value)} />

                <button className="custom-btn-2" onClick={() => handleSearchClick()}>
                    Search
                </button>

            </div>

            {isLoading ? <LoadingSpinner /> :

                <>

                    {httpError ? <HttpErrorMessage httpError={httpError} /> :
                        
                        <>

                            {totalAmountOfBooks === 0 ? <div>Nothing was found</div> :
                                
                                <>

                                    <PaginatedItemsCount itemsName={"Books"} totalAmountOfItems={totalAmountOfBooks} resultRange={resultRange} />
                                
                                    {books.map(
                                        
                                        book => <QuantitiesTabBookCard key={book.id} book={book} setIsBookDeleted={setIsBookDeleted} />
                                        
                                    )}

                                </>

                            }

                        </>

                    }

                </>

            }

            <Pagination currentPage={currentPage} totalPages={totalPages} totalAmountOfItems={totalAmountOfBooks} 
                setCurrentPage={setCurrentPage} setResultRange={setResultRange} />
            
        </div>

    )

}