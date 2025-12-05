import { useState } from "react";
import { Quote } from "../../commons/quote/Quote"
import { ReviewModel } from "../../../models/ReviewModel";
import { useFetchBookReviews } from "../../../utils/api_fetchers/review_controller/useFetchBookReviews";
import { Pagination } from "../../commons/pagination/Pagination";
import { ReviewCard } from "../../commons/review_card/ReviewCard";
import { Link } from "react-router-dom";
import { LoadingSpinner } from "../../commons/loading_spinner/LoadingSpinner";
import { PaginatedItemsCount } from "../../commons/pagination/PaginatedItemsCount";
import { HttpErrorMessage } from "../../commons/http_error_message/HttpErrorMessage";

export const ReviewsPage = () => {

    const bookId = (window.location.pathname).split('/')[2];

    const [reviews, setReviews] = useState<ReviewModel[]>([]);
    const [totalAmountOfReviews, setTotalAmountOfReviews] = useState(0);
    const [isLoadingReviews, setIsLoadingReviews] = useState(true);
    const [reviewsHttpError, setReviewsHttpError] = useState<string | null>(null);

    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotlalPages] = useState(0);
    const [resultRange, setResultRange] = useState({start: 1, end: 5});

    const urlPaginationParams = `?page=${currentPage - 1}&reviews-per-page=5`;

    useFetchBookReviews(bookId, setReviews, setIsLoadingReviews, setReviewsHttpError, setTotalAmountOfReviews, urlPaginationParams, currentPage, setTotlalPages);

    return (

        <div className="page-container">

            <Quote quoteId={7} />

            <div className="w-full p-5 flex flex-col gap-5">

                {isLoadingReviews ? <LoadingSpinner /> :

                    <>

                        {reviewsHttpError ? <HttpErrorMessage httpError={reviewsHttpError} /> :

                            <div className="flex flex-col gap-5">

                                <div className="flex items-center justify-between gap-5 max-md:flex-col">

                                    <PaginatedItemsCount itemsName={"Reviews"} totalAmountOfItems={totalAmountOfReviews} resultRange={resultRange} />

                                    <Link to={`/book/${bookId}`} className="custom-btn-1">Back to book</Link>

                                </div>

                                {reviews.map(

                                    review => <ReviewCard key={review.id} review={review} />

                                )}

                            </div>

                        }

                    </>

                }

            </div>

            <Pagination currentPage={currentPage} totalPages={totalPages} totalAmountOfItems={totalAmountOfReviews} 
                setCurrentPage={setCurrentPage} setResultRange={setResultRange} 
            />

        </div>

    )

}