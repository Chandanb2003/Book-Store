import { ReviewModel } from "../../../../models/ReviewModel";
import { ReviewCard } from "../../../commons/review_card/ReviewCard";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { Link } from "react-router-dom";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

type LatestReviewsProps = {
    bookId: string
    reviews: ReviewModel[],
    totalAmountOfReviews: number,
    isLoadingReviews: boolean,
    reviewsHttpError: string | null,
}

export const LatestReviews = ({ bookId, reviews, totalAmountOfReviews, isLoadingReviews, reviewsHttpError }: LatestReviewsProps) => {

    return (

        <div className="w-full p-5 flex flex-col gap-5">

            {totalAmountOfReviews !== 0 ? 

                <>

                    <div className="flex gap-3 items-center font-semibold lg:text-2xl max-lg:text-xl max-lg:self-center">
                        
                        Latest Reviews: 
                        
                        <span className="text-teal-600 lg:text-3xl max-lg:text-2xl">{totalAmountOfReviews < 3 ? totalAmountOfReviews : 3}</span> 
                        
                        out of 
                        
                        <span className="text-teal-600 lg:text-3xl max-lg:text-2xl">{totalAmountOfReviews}</span>
                        
                    </div>

                    {isLoadingReviews ? <LoadingSpinner /> :

                        <>

                            {reviewsHttpError ? <HttpErrorMessage httpError={reviewsHttpError} /> :

                                <div className="flex flex-col gap-5">

                                    {reviews.map(

                                        (review) => <ReviewCard key={review.id} review={review} />

                                    )}

                                    <Link to={`/reviews/${bookId}`} className="custom-btn-1 self-start">All reviews</Link>

                                </div>

                            }

                        </>

                    }

                </>

                :

                <p className="font-semibold text-center lg:text-2xl max-lg:text-xl">No reviews yet. Be the first one to rate this book and leave a comment!</p>
                
            }

        </div>

    )

}