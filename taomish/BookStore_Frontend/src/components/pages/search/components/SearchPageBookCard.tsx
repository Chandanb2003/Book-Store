import { Link } from "react-router-dom"
import { BookModel } from "../../../../models/BookModel"
import { BookGenres } from "../../../commons/book_genres/BookGenres"

type BookCardProps = {
    book: BookModel
}

export const SearchPageBookCard = ({ book }: BookCardProps) => {

    return (

        <div className="book-card relative">

            <img src={book.img} alt="cover" width={250} height={400} className="shadow-xl max-lg:w-[200px]"/>
            
            <div className="flex flex-col gap-10 lg:w-1/2">

                <div className="max-lg:text-center">
                
                    <p className="font-semibold lg:text-3xl max-lg:text-2xl">{book.title}</p>
                    <p className="font-light lg:text-2xl max-lg:text-xl">{book.author}</p>

                </div>

                <BookGenres genres={book.genres} />

                <div className="max-lg:text-center">

                    {book.description}

                </div>
            
            </div>

            <Link to={`/book/${book.id}`} className="custom-btn-1 max-lg:static lg:absolute lg:bottom-10 lg:right-10">
                See Details
            </Link>

        </div>

    )

}