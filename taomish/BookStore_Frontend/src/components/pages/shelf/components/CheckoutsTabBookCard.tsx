import { CheckoutModel } from "../../../../models/CheckoutModel"
import { BookGenres } from "../../../commons/book_genres/BookGenres"
import { CheckoutOptionsBox } from "./CheckoutOptionsBox"

type CheckoutsTabBookCardProps = {
    checkout: CheckoutModel,
    setIsBookReturned: React.Dispatch<React.SetStateAction<boolean>>,
    setIsCheckoutRenewed: React.Dispatch<React.SetStateAction<boolean>>
}

export const CheckoutsTabBookCard = ({ checkout, setIsBookReturned, setIsCheckoutRenewed }: CheckoutsTabBookCardProps) => {

    return (

        <div className="book-card">

            <img src={checkout.bookDTO.img} alt="cover" width={200} height={320} className="shadow-xl" />

            <div className="flex flex-col gap-10 xl:w-5/12 lg:flex-1 w-full">

                <div className="max-lg:text-center">
                
                    <p className="font-semibold lg:text-2xl max-lg:text-xl">{checkout.bookDTO.title}</p>
                    <p className="font-light lg:text-xl max-lg:text-lg">{checkout.bookDTO.author}</p>

                </div>

                <BookGenres genres={checkout.bookDTO.genres} />
            
            </div>

            <CheckoutOptionsBox checkout={checkout} setIsBookReturned={setIsBookReturned} setIsCheckoutRenewed={setIsCheckoutRenewed} />
            
        </div>

    )

}