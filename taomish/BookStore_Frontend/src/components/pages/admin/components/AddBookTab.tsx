import { useState } from "react";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { BookModel } from "../../../../models/BookModel";
import { FormLoader } from "../../../commons/form_loader/FormLoader";
import { FieldErrors } from "../../../commons/field_errors/FieldErrors";
import { GenreModel } from "../../../../models/GenreModel";
import { useAddNewBook } from "../../../../utils/api_fetchers/admin_controller/useAddNewBook";
import { useFetchAllGenres } from "../../../../utils/api_fetchers/genre_controller/useFetchAllGenres";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

export const AddBookTab = () => {

    const { authentication } = useAuthenticationContext();

    const [newBook, setNewBook] = useState<BookModel>({ title: "", author: "", description: "", copies: 0, copiesAvailable: 0, genres: [], img: "" });
    const [isLoadingSubmit, setIsLoadingSubmit] = useState(false);
    const [bookSubmitHttpError, setBookSubmitHttpError] = useState<string | null>(null);
    const [displaySuccess, setDisplaySuccess] = useState(false);
    
    const [allGenres, setAllGenres] = useState<GenreModel[]>([]);
    const [isLoadingGenres, setIsLoadingGenres] = useState(true);
    const [genresHttpError, setGenresHttpError] = useState<string | null>(null);

    useFetchAllGenres(setAllGenres, setIsLoadingGenres, setGenresHttpError);

    async function base64ImgConversion(e: any) {

        const file = e.target.files[0];

        if (file) {

            let reader = new FileReader();

            reader.readAsDataURL(file);

            reader.onload = function () {

                const convertedImg = reader.result?.toString();
                if (convertedImg) setNewBook({ ...newBook, img: convertedImg });
            };

            reader.onerror = function (error) {
                console.log("Error", error)
            }
        }
    }

    const handleChange = (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => {

        setNewBook({ ...newBook, [event.target.name]: event.target.value });
    };

    const handleGenreClick = (genre: GenreModel) => {

        if (newBook.genres.length === 0) {
            setNewBook({ ...newBook, genres: [genre] });
        } else if (newBook.genres.includes(genre)) {
            setNewBook({ ...newBook, genres: newBook.genres.filter(item => item !== genre) });
        } else {
            setNewBook({ ...newBook, genres: [...newBook.genres, genre] });
        }
    }

    const handleSubmitBookClick = async () => {

        await useAddNewBook(authentication, newBook, setNewBook, setIsLoadingSubmit, setBookSubmitHttpError, setDisplaySuccess);
    };

    return (

        <div className="custom-form max-w-full">

            {displaySuccess && 
                
                <div className="text-lg font-semibold bg-green-200 rounded-md px-5 py-1">
                    New book is added successfully!
                </div>
            
            }

            <p className="text-center text-3xl max-lg:text-2xl font-semibold">Add Book</p>

            <FormLoader isLoading={isLoadingSubmit} />

            {(bookSubmitHttpError && !bookSubmitHttpError.startsWith("Some")) && <HttpErrorMessage httpError={bookSubmitHttpError} />}

            <form className="flex flex-col gap-5 w-full">

                <div className="flex max-lg:flex-col gap-5 w-full">
                    
                    <div className="flex flex-col gap-1 lg:w-7/12">

                        <FieldErrors fieldName="title" httpError={bookSubmitHttpError} />
                        <input type="text" name="title" value={newBook.title} onChange={handleChange} placeholder="Book title" className="input shadow-md"/>
                    
                    </div>

                    <div className="flex flex-col gap-1 lg:w-5/12">

                        <FieldErrors fieldName="author" httpError={bookSubmitHttpError} />
                        <input type="text" name="author" value={newBook.author} onChange={handleChange} placeholder="Author" className="input shadow-md"/>

                    </div>

                </div>

                <div className="flex flex-col gap-1">

                    <FieldErrors fieldName="description" httpError={bookSubmitHttpError} />
                    <textarea rows={3} name="description" value={newBook.description} onChange={handleChange} placeholder="Book description" className="input shadow-md"/>

                </div>

                <div className="flex max-lg:flex-col gap-5 w-full lg:items-center">
                
                    <div className="flex flex-col gap-1 lg:w-3/12">

                        <FieldErrors fieldName="copies" httpError={bookSubmitHttpError} />

                        <div className="flex gap-5 items-center whitespace-nowrap pl-1">

                            Copies :

                            <input type="number" name="copies" value={newBook.copies} onChange={handleChange} className="input shadow-md"/>
                        
                        </div>

                    </div>

                    <div className="flex flex-col gap-1 lg:w-4/12">

                        <FieldErrors fieldName="copiesAvailable" httpError={bookSubmitHttpError} />

                        <div className="flex gap-5 items-center whitespace-nowrap pl-1">

                            Copies Available :

                            <input type="number" name="copiesAvailable" value={newBook.copiesAvailable} onChange={handleChange} className="input shadow-md"/>
                        
                        </div>

                    </div>

                    <div className="flex flex-col gap-1 lg:w-5/12">

                        <FieldErrors fieldName="img" httpError={bookSubmitHttpError} />
                        <input type="file" name="img" onChange={base64ImgConversion} className="input shadow-md text-base"/>
                    
                    </div>

                </div>

                <div className="flex flex-col gap-1">

                    <FieldErrors fieldName="genres" httpError={bookSubmitHttpError} />

                    <div className="flex max-lg:flex-col items-center gap-5 p-5 rounded-md border-2 border-teal-600 bg-white">

                        Select genres:

                        {isLoadingGenres ? <LoadingSpinner /> :

                            <>

                                {genresHttpError ? <HttpErrorMessage httpError={genresHttpError} /> : 
                            
                                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-5">

                                        {allGenres.map(

                                            genre => (

                                                <label key={genre.id} className="border-2 border-teal-600 rounded-md p-2 bg-teal-50 flex gap-2 items-center">

                                                    <input type="checkbox" className="checkbox" onClick={() => handleGenreClick(genre)} />

                                                    {genre.description}

                                                </label>
                                            )
                                        )}

                                    </div>

                                }

                            </>

                        }

                    </div>

                </div>

            </form>

            <button className="custom-btn-2" onClick={handleSubmitBookClick}>Submit Book</button>

        </div>

    )

}