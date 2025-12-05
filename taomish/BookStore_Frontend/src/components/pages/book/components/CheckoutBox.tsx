import { useState } from "react";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { useFetchCurrentCheckoutsCount } from "../../../../utils/api_fetchers/checkout_controller/useFetchCurrentCheckoutsCount";
import { useCheckIfBookCheckedOutByUser } from "../../../../utils/api_fetchers/book_controller/useCheckIfBookCheckedOutByUser";
import { Link } from "react-router-dom";
import { useCheckIfBookReviewedByUser } from "../../../../utils/api_fetchers/book_controller/useCheckIfBookReviewedByUser";
import { FormLoader } from "../../../commons/form_loader/FormLoader";
import { ReviewFormBox } from "./ReviewFormBox";
import { useSubmitReview } from "../../../../utils/api_fetchers/book_controller/useSubmitReview";
import { ReviewModel } from "../../../../models/ReviewModel";
import { useCheckOutBook } from "../../../../utils/api_fetchers/book_controller/useCheckOutBook";
import { BookModel } from "../../../../models/BookModel";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

type CheckoutBoxProps = { 
    book: BookModel,
    setIsRatingChanged: React.Dispatch<React.SetStateAction<boolean>>
};

export const CheckoutBox = ({ book, setIsRatingChanged }: CheckoutBoxProps) => {

    const { authentication } = useAuthenticationContext();
    const bookId: string = `${book.id}`;

    const [copiesAvailable, setCopiesAvailable] = useState(book.copiesAvailable);

    // Loans count state
    const [currentCheckoutsCount, setCurrentCheckoutsCount] = useState(0);
    const [isLoadingCurrentCheckoutsCount, setIsLoadingCurrentCheckoutsCount] = useState(true);
    const [currentCheckoutsCountHttpError, setCurrentCheckoutsCountHttpError] = useState<string | null>(null);

    // Is book checked out by user state
    const [isCheckedOut, setIsCheckedOut] = useState(false);
    const [isLoadingBookCheckedOut, setIsLoadingBookCheckedOut] = useState(true);
    const [isCheckedOutHttpError, setIsCheckedOutHttpError] = useState<string | null>(null);
    const [checkOutHttpError, setCheckOutHttpError] = useState<string | null>(null);

    // User review state
    const [isReviewLeft, setIsReviewLeft] = useState(false);
    const [isLoadingUserReview, setIsLoadingUserReview] = useState(true);
    const [userReviewLeftHttpError, setUserReviewLeftHttpError] = useState<string | null>(null);
    const [userReviewSubmitHttpError, setUserReviewSubmitHttpError] = useState<string | null>(null);

    useFetchCurrentCheckoutsCount(authentication, setCurrentCheckoutsCount, setIsLoadingCurrentCheckoutsCount, setCurrentCheckoutsCountHttpError);

    useCheckIfBookCheckedOutByUser(bookId, authentication, setIsCheckedOut, setIsLoadingBookCheckedOut, setIsCheckedOutHttpError);

    useCheckIfBookReviewedByUser(bookId, authentication, setIsReviewLeft, setIsLoadingUserReview, setUserReviewLeftHttpError);

    const handleCheckoutClick = async () => {

        await useCheckOutBook(bookId, authentication, setIsLoadingBookCheckedOut, setCheckOutHttpError, setCopiesAvailable, setIsCheckedOut, setCurrentCheckoutsCount);
    };

    const handleSubmitReviewClick = async (review: ReviewModel) => {

        await useSubmitReview(bookId, authentication, review, setIsLoadingUserReview, setIsReviewLeft, setUserReviewSubmitHttpError, setIsRatingChanged);
    };

    const renderBooksCheckedOut = () => {

        if (authentication.isAuthenticated) {
            
            if (isLoadingCurrentCheckoutsCount) return <FormLoader isLoading={true} />

            if (currentCheckoutsCountHttpError) return <HttpErrorMessage httpError={currentCheckoutsCountHttpError} />

            return <p className="text-lg">Books checked out: <span className="text-teal-600 text-xl font-semibold">{currentCheckoutsCount} / 5 </span></p>
        }

        return <p className="text-lg font-semibold">Sign in to check this book out!</p>
    }

    const renderCheckoutBtn = () => {

        if (authentication.isAuthenticated) {
            
            if (isLoadingBookCheckedOut) return <FormLoader isLoading={true} />

            if (isCheckedOutHttpError) return <HttpErrorMessage httpError={isCheckedOutHttpError} />

            if (!isCheckedOut) {

                if (currentCheckoutsCount < 5) {
                    
                    return <div className="flex flex-col items-center gap-1">

                        {checkOutHttpError && <HttpErrorMessage httpError={checkOutHttpError} />}
                    
                        <button className="custom-btn-1" onClick={handleCheckoutClick}>Checkout</button>

                    </div>

                }

                return <p className="text-red-600 text-lg font-semibold">To many books checked out.</p>
            }

            return <p className="text-green-600 text-lg font-semibold">Book checked out. Enjoy!</p>

        }

        return <Link to={'/login'} className="custom-btn-1">Sign in</Link>
    }

    const renderReviewBtn = () => {

        if (authentication.isAuthenticated) {
            
            if (isLoadingUserReview) return <FormLoader isLoading={true} />

            if (userReviewLeftHttpError) return <HttpErrorMessage httpError={userReviewLeftHttpError} />

            if (isReviewLeft) return <p className="text-lg font-semibold">Thank you for your review!</p>

            return <ReviewFormBox handleSubmitReviewClick={handleSubmitReviewClick} userReviewSubmitHttpError={userReviewSubmitHttpError} />

        }

        return <p className="text-lg font-semibold">Sign in to be able to leave a review!</p>
    }

    return (

        <div className="book-card-options-box">

            {renderBooksCheckedOut()}

            <div className="divider-2" />

            {copiesAvailable && copiesAvailable > 0 ?
                        
                <p className="text-green-600 text-2xl font-semibold">Available</p>

                :

                <p className="text-red-600 text-2xl font-semibold">Wait List</p>
            }

            <div className="flex gap-5 text-lg">

                <p><span className="font-semibold">{book.copies}</span> copies</p>

                <p><span className="font-semibold">{copiesAvailable}</span> available</p>

            </div>

            {renderCheckoutBtn()}

            <div className="divider-2" />
            
            <p className="font-light text-center">
                Number of books available may change until checkout is complete.
            </p>
            
            <div className="divider-2" />

            {renderReviewBtn()}
            
        </div>

    )

}