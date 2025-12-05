import { useState } from "react"
import { BookModel } from "../../../../models/BookModel"
import { ReviewStars } from "../../../commons/review_stars/ReviewStars"
import { CheckoutBox } from "./CheckoutBox"
import { useFetchBookAverageRating } from "../../../../utils/api_fetchers/review_controller/useFetchBookAverageRating"
import { FormLoader } from "../../../commons/form_loader/FormLoader"
import { BookGenres } from "../../../commons/book_genres/BookGenres"
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage"

type BookCardProps = {
    book: BookModel
}

export const BookPageBookCard = ({ book }: BookCardProps) => {

    const [averageRating, setAverageRating] = useState(0);
    const [isLoadingAverageRating, setIsLoadingAverageRating] = useState(true);
    const [averageRatingHttpError, setAverageRatingHttpError] = useState<string | null>(null);
    const [isRatingChanged, setIsRatingChanged] = useState(false);

    useFetchBookAverageRating(`${book.id}`, setAverageRating, setIsLoadingAverageRating, setAverageRatingHttpError, isRatingChanged);

    return (

        <div className="book-card">

            <img src={book.img} alt="cover" width={250} height={400} className="shadow-xl"/>
            
            <div className="flex flex-col gap-10 xl:w-5/12 lg:flex-1">

                <div className="max-lg:text-center">
                
                    <p className="font-semibold lg:text-3xl max-lg:text-2xl">{book.title}</p>
                    <p className="font-light lg:text-2xl max-lg:text-xl">{book.author}</p>

                </div>

                <BookGenres genres={book.genres} />

                <div className="flex max-lg:justify-center">

                    {isLoadingAverageRating ? <FormLoader isLoading={isLoadingAverageRating} /> :

                        <>

                            {averageRatingHttpError ? <HttpErrorMessage httpError={averageRatingHttpError} /> :

                                <ReviewStars ratingProp={averageRating} size={25} />

                            }

                        </>
                    
                    }
                    
                </div>

                <div className="max-lg:text-center">

                    {book.description}

                </div>
            
            </div>
            
            <CheckoutBox book={book} setIsRatingChanged={setIsRatingChanged} />

        </div>

    )

}