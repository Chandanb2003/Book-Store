import { useState } from "react";
import { ReviewStars } from "../../../commons/review_stars/ReviewStars"
import { ratings } from "../../../../constants/constants";
import { ReviewModel } from "../../../../models/ReviewModel";
import { FieldErrors } from "../../../commons/field_errors/FieldErrors";

type ReviewFormBoxProps = {
    handleSubmitReviewClick: (review: ReviewModel) => Promise<void>,
    userReviewSubmitHttpError: string | null
}

export const ReviewFormBox = ({ handleSubmitReviewClick, userReviewSubmitHttpError }: ReviewFormBoxProps) => {

    const [review, setReview] = useState<ReviewModel>({ personEmail: "", personFirstName: "", date: new Date(), rating: 0, reviewDescription: "" });

    const handleRatingChange = (value: string) => {

        setReview({ ...review, rating: Number(value) });
    };

    const handleCommentChange = (value: string) => {

        setReview({ ...review, reviewDescription: value });
    };

    const handleSubmit = () => {

        handleSubmitReviewClick(review);
    }

    return (

        <div className="flex flex-col gap-5 items-center w-full">

            <p className="w-full text-lg font-semibold text-center">Leave a review here:</p>

            <FieldErrors fieldName={"rating"} httpError={userReviewSubmitHttpError} />

            <div className="w-full flex gap-3 items-center justify-center">

                <ReviewStars ratingProp={review.rating} size={20} />

                <select className="dropdown p-1" value={review.rating} onChange={event => handleRatingChange(event.target.value)}>

                    <option disabled value="0">Rate</option>

                    {ratings.map(

                        rating => <option key={rating.id} value={rating.value}>{rating.name}</option>

                    )}

                </select>

            </div>

            <textarea className="input" rows={1} placeholder="Leave a comment (optional)..." value={review.reviewDescription} onChange={event => handleCommentChange(event.target.value)} />

            <button className="custom-btn-2" onClick={handleSubmit}>
                Submit a review
            </button>
            
        </div>

    )

}