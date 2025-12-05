import { useState } from "react"
import { useAuthenticationContext } from "../../../../authentication/authenticationContext"
import { CheckoutModel } from "../../../../models/CheckoutModel"
import { useRenewCheckout } from "../../../../utils/api_fetchers/book_controller/useRenewCheckout"
import { useReturnBook } from "../../../../utils/api_fetchers/book_controller/useReturnBook"
import { FormLoader } from "../../../commons/form_loader/FormLoader"
import { Link } from "react-router-dom"
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage"

type CheckoutOptionsBoxProps = {
    checkout: CheckoutModel,
    setIsBookReturned: React.Dispatch<React.SetStateAction<boolean>>,
    setIsCheckoutRenewed: React.Dispatch<React.SetStateAction<boolean>>
}

export const CheckoutOptionsBox = ({ checkout, setIsBookReturned, setIsCheckoutRenewed }: CheckoutOptionsBoxProps) => {

    const { authentication } = useAuthenticationContext();

    const [isLoadingReturnBook, setIsLoadingReturnBook] = useState(false);
    const [returnBookHttpError, setReturnBookHttpError] = useState<string | null>(null);

    const [isLoadingRenewCheckout, setIsLoadingRenewCheckout] = useState(false);
    const [renewCheckoutHttpError, setRenewCheckoutHttpError] = useState<string | null>(null);

    const handleReturnBookClick = (bookId: number | undefined) => {

        useReturnBook(`${bookId}`, authentication, setIsLoadingReturnBook, setReturnBookHttpError, setIsBookReturned);
    }

    const handleRenewCheckoutClick = (bookId: number | undefined) => {

        useRenewCheckout(`${bookId}`, authentication, setIsLoadingRenewCheckout, setRenewCheckoutHttpError, setIsCheckoutRenewed);
    }

    const renderDueDays = () => {

        if (checkout.daysLeft > 0) {
            
            return <p className="text-green-600 text-lg font-semibold">Due in {checkout.daysLeft} days.</p>
        
        } else if (checkout.daysLeft === 0) {
            
            return <p className="text-amber-500 text-lg font-semibold">Due today.</p>

        } else {
            
            return <p className="text-red-600 text-lg font-semibold">Overdue by {checkout.daysLeft} days.</p>
        }
    }

    return (

        <div className="book-card-options-box">

            <p className="text-xl font-semibold">Checkout options</p>

            <div className="divider-2" />

            {renderDueDays()}

            <div className="flex flex-col gap-5">

                {(isLoadingReturnBook || isLoadingRenewCheckout) ? <FormLoader isLoading={true} /> :

                    <>

                        {returnBookHttpError && <HttpErrorMessage httpError={returnBookHttpError} />}

                        {renewCheckoutHttpError && <HttpErrorMessage httpError={renewCheckoutHttpError} /> }

                        <div className="flex gap-3 max-xl:flex-col">

                            <button className="custom-btn-1" onClick={() => handleReturnBookClick(checkout.bookDTO.id)}>
                                Return book
                            </button>

                            <button className="custom-btn-1" onClick={() => handleRenewCheckoutClick(checkout.bookDTO.id)}>
                                Renew for 7 days
                            </button>

                        </div>

                    </>

                }

                <Link to={'/search'} className="custom-btn-2 text-center">
                    Search for more books
                </Link>

            </div>

            <div className="divider-2" />

            <p className="text-center">Help others find their adventure by reviewing this book.</p>

            <Link to={`/book/${checkout.bookDTO.id}`} className="custom-btn-2 text-center">
                Leave a review
            </Link>

        </div>

    )

}