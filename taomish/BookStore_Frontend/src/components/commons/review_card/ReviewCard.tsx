import { ReviewModel } from "../../../models/ReviewModel";
import { ReviewStars } from "../review_stars/ReviewStars";
import avatar from "../../../assets/icons/avatar.svg";

type ReviewCardProps = {
    review: ReviewModel
}

export const ReviewCard = ({ review }: ReviewCardProps) => {

    const date = new Date(review.date);

    const longMonth = date.toLocaleDateString("en-us", { month: "long" });
    const dateDay = date.getDate();
    const dateYear = date.getFullYear();

    const dateRender = longMonth + " " + dateDay + ", " + dateYear;

    return (

        <div className="flex flex-col gap-5 p-5 rounded-lg shadow-custom-3">

            <div className="flex max-md:flex-col justify-between md:items-center">
                
                <div className="flex gap-4 items-center text-lg">
                    
                    <img src={avatar} alt="avatar" width={60} height={60}  />

                    <div className="flex gap-3 max-lg:flex-col max-lg:gap-0">

                        <p className="font-bold">{review.personFirstName}</p>

                        <p className="font-light">{review.personEmail}</p>

                    </div>

                </div>
                
                <div className="flex items-center gap-3 max-md:self-center max-md:hidden">

                    <p>{dateRender}</p>

                    <ReviewStars ratingProp={review.rating} size={20} />

                </div>

            </div>

            <div className="divider-2" />

            <div className="flex items-center gap-3 max-md:self-center md:hidden">

                <p>{dateRender}</p>

                <ReviewStars ratingProp={review.rating} size={20} />

            </div>

            <div className="text-lg">
                        
                {review.reviewDescription ? `"${review.reviewDescription}"` : <p className=" opacity-70">This person did not leave a comment</p>}

            </div>

        </div>

    )

}