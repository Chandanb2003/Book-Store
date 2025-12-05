import { GenreModel } from "../../../models/GenreModel"

type BookGenresProps ={
    genres: GenreModel[]
}

export const BookGenres = ({ genres }: BookGenresProps) => {

    return (

        <div className="auto-grid gap-2 w-full">

            {genres.map(

                (genre) => <div key={genre.description} className="bg-teal-100 py-1 px-2 rounded-md text-center">{genre.description}</div>

            )}

        </div>

    )

}