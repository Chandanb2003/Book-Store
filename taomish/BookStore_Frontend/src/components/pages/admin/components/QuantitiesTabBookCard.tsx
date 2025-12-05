import { BookModel } from "../../../../models/BookModel"
import { BookGenres } from "../../../commons/book_genres/BookGenres";
import { ManageBookOptionsBox } from "./ManageBookOptionsBox";

type BookCardProps = {
    book: BookModel,
    setIsBookDeleted: React.Dispatch<React.SetStateAction<boolean>>
}

export const QuantitiesTabBookCard = ({ book, setIsBookDeleted }: BookCardProps) => {

    return (

        <div className="book-card">

            <img src={book.img} alt="cover" width={200} height={320} className="shadow-xl max-lg:w-[200px]"/>
            
            <div className="flex flex-col gap-10 max-lg:gap-5 xl:w-5/12 lg:flex-1 w-full">

                <div className="max-lg:text-center">
                
                    <p className="font-semibold lg:text-2xl max-lg:text-xl">{book.title}</p>
                    <p className="font-light lg:text-xl max-lg:text-lg">{book.author}</p>

                </div>

                <BookGenres genres={book.genres} />
            
            </div>

            <ManageBookOptionsBox book={book} setIsBookDeleted={setIsBookDeleted} />

        </div>

    )

}