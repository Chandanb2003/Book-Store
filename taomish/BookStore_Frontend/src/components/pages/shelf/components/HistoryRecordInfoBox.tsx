import { Link } from "react-router-dom"
import { HistoryRecordModel } from "../../../../models/HistoryRecordModel"

type HistoryRecordInfoBoxProps = {
    record: HistoryRecordModel
}

export const HistoryRecordInfoBox = ({ record }: HistoryRecordInfoBoxProps) => {

    const renderDate = (dateValue: Date) => {

        const date = new Date(dateValue);

        const longMonth = date.toLocaleDateString("en-us", { month: "long" });
        const dateDay = date.getDate();
        const dateYear = date.getFullYear();

        return longMonth + " " + dateDay + ", " + dateYear;
    }

    return (

        <div className="book-card-options-box">

            <p className="text-xl font-semibold">History record info</p>

            <div className="divider-2" />

            <div className="flex gap-1 items-center text-lg text-center">

                <p>Checked:</p>
                
                <p className="text-teal-600 font-semibold"> {renderDate(record.checkoutDate)}</p>

            </div>

            <div className="flex gap-1 items-center text-lg text-center">

                <p>Returned:</p>
                
                <p className="text-teal-600 font-semibold"> {renderDate(record.returnDate)}</p>

            </div>

            <div className="divider-2" />

            <p className="text-center font-light">Help others find their adventure by reviewing this book or find more exciting books in our collection.</p>

            <div className="flex max-xl:flex-col gap-5 items-center">

                <Link to={`/book/${record.bookDTO.id}`} className="custom-btn-2 text-center">
                    Leave a review
                </Link>
                
                <Link to={'/search'} className="custom-btn-2 text-center">
                    Search for more books
                </Link>

            </div>

        </div>

    )

}