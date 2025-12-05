import { HistoryRecordModel } from "../../../../models/HistoryRecordModel"
import { BookGenres } from "../../../commons/book_genres/BookGenres"
import { HistoryRecordInfoBox } from "./HistoryRecordInfoBox"

type HistoryTabRecordCardProps = {
    record: HistoryRecordModel
}

export const HistoryTabRecordCard = ({ record }: HistoryTabRecordCardProps) => {

    return (

        <div className="book-card">

            <img src={record.bookDTO.img} alt="cover" width={200} height={320} className="shadow-xl" />

            <div className="flex flex-col gap-10 max-lg:gap-5 xl:w-5/12 lg:flex-1 w-full">

                <div className="max-lg:text-center">
                
                    <p className="font-semibold lg:text-2xl max-lg:text-xl">{record.bookDTO.title}</p>
                    <p className="font-light lg:text-xl max-lg:text-lg">{record.bookDTO.author}</p>

                </div>

                <BookGenres genres={record.bookDTO.genres} />
            
            </div>

            <HistoryRecordInfoBox record={record} />
            
        </div>

    )

}