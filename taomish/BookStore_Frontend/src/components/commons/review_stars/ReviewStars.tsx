import fullBall from "../../../assets/icons/rating-full-ball.svg";
import halfBall from "../../../assets/icons/rating-half-ball.svg";
import emptyBall from "../../../assets/icons/rating-empty-ball.svg";

type ReviewStarsProps = {
    ratingProp: number
    size: number
}

export const ReviewStars = ({ ratingProp, size }: ReviewStarsProps) => {

    let rating = ratingProp;

    let fullStars = 0;
    let halfStars = 0;
    let emptyStars = 0;

    if (rating !== undefined && rating > 0 && rating <=5) {

        for (let i = 0; i <=4; i++) {

            if (rating - 1 >= 0) {

                fullStars = fullStars + 1;
                rating = rating - 1;

            } else if (rating === .5) {

                halfStars = halfStars + 1;
                rating = rating - .5;

            } else if (rating == 0) {

                emptyStars = emptyStars + 1;

            } else break;

        }

    } else emptyStars = 5;

    return (

        <div className="flex gap-1">

            {Array.from(
                
                { length: fullStars }, (_, i) => <img key={i} src={fullBall} alt="full-ball" width={size} height={size} />
            )}

            {Array.from(
                
                { length: halfStars }, (_, i) => <img key={i} src={halfBall} alt="half-ball" width={size} height={size} />
            )}

            {Array.from(
                
                { length: emptyStars }, (_, i) => <img key={i} src={emptyBall} alt="empty-ball" width={size} height={size} />
            )}

        </div>

    )

}