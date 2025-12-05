import { useState } from "react";
import { useFetchAllGenres } from "../../../../utils/api_fetchers/genre_controller/useFetchAllGenres";
import { GenreModel } from "../../../../models/GenreModel";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

type SearchPanelProps = {
    selectedGenre: string,
    handleGenreChange: (value: string) => void,
    titleQuery: string,
    setTitleQuery: React.Dispatch<React.SetStateAction<string>>,
    handleSearchClick: () => void
}

export const SearchPanel = ({ selectedGenre, handleGenreChange, titleQuery, setTitleQuery, handleSearchClick }: SearchPanelProps) => {

    const [allGenres, setAllGenres] = useState<GenreModel[]>([]);
    const [isLoadingGenres, setIsLoadingGenres] = useState(true);
    const [genresHttpError, setGenresHttpError] = useState<string | null>(null);

    useFetchAllGenres(setAllGenres, setIsLoadingGenres, setGenresHttpError);

    const renderGenresDropdown = () => {

        return <>

            {genresHttpError ? <HttpErrorMessage httpError={genresHttpError} /> : 

                <select className="dropdown" value={selectedGenre} onChange={event => handleGenreChange(event.target.value)}>

                    <option disabled value="">Search by genre</option>

                    {allGenres.map(

                        genre => <option key={genre.id} value={genre.description}>{genre.description}</option>

                    )}

                    <option value="">All genres</option>

                </select>

            }

        </>
    };

    return (

        <>

            {/* Desktop Search panel */}

            <div className="flex w-full gap-5 max-sm:hidden">

                {isLoadingGenres ? <LoadingSpinner /> : renderGenresDropdown()}

                <input className="input" placeholder="Search books by title..." value={titleQuery} onChange={event => setTitleQuery(event.target.value)} />

                <button className="custom-btn-2" onClick={() => handleSearchClick()}>
                    Search
                </button>

            </div>


            {/* Mobile Search panel */}

            <div className="flex flex-col w-full gap-5 sm:hidden">
                
                <input className="input" placeholder="Search books by title..." value={titleQuery} onChange={event => setTitleQuery(event.target.value)} />

                <div className="flex w-full justify-between">

                    {isLoadingGenres ? <LoadingSpinner /> : renderGenresDropdown()}

                    <button className="custom-btn-2" onClick={() => handleSearchClick()}>
                        Search
                    </button>
                    
                </div>

            </div>

        </>

    )

}