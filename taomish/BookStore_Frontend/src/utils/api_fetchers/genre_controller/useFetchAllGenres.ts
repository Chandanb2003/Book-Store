import { useEffect } from "react";
import { GenreModel } from "../../../models/GenreModel";
import { genre_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchAllGenres = (setAllGenres: React.Dispatch<React.SetStateAction<GenreModel[]>>,
                                  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                  setHttpError: React.Dispatch<React.SetStateAction<string | null>>) => {

    useEffect(

        () => {

            const fetchGenres = async () => {

                const endpoint = genre_controller_endpoints.find_all_genres;

                const url = endpoint.url;

                const response = await fetch(url);

                const responseJson = await response.json();

                if (!response.ok) {
                    throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                }

                const loadedGenres: GenreModel[] = [];

                for (const key in responseJson) {

                    loadedGenres.push(responseJson[key]);
                }

                setAllGenres(loadedGenres);
                setIsLoading(false);
            }

            fetchGenres().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, []

    );

}