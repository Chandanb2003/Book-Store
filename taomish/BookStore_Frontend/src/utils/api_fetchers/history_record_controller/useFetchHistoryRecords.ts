import { useEffect } from "react";
import { HistoryRecordModel } from "../../../models/HistoryRecordModel";
import { history_record_controller_endpoints } from "../../apiEndpointsUrlsList";

export const useFetchHistoryRecords = (authentication: { isAuthenticated: boolean; token: string; },
                                       setHistoryRecords: React.Dispatch<React.SetStateAction<HistoryRecordModel[]>>,
                                       setTotalAmountOfRecords: React.Dispatch<React.SetStateAction<number>>,
                                       setTotlalPages: React.Dispatch<React.SetStateAction<number>>,
                                       setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
                                       setHttpError: React.Dispatch<React.SetStateAction<string | null>>,
                                       currentPage: number) => {

    useEffect(

        () => {

            const fetchUserHistoryRecords = async () => {

                setIsLoading(true);

                if (authentication.isAuthenticated) {

                    const urlParams = `?page=${currentPage - 1}&records-per-page=5`;

                    const endpoint = history_record_controller_endpoints.find_all_history_records;

                    const url = endpoint.url + urlParams;

                    const requestOptions = {

                        method: endpoint.method,
                        headers: {
                            Authorization: `Bearer ${authentication.token}`,
                            "Content-type": "application/json"
                        }
                    };

                    const response = await fetch(url, requestOptions);

                    const responseJson = await response.json();

                    if (!response.ok) {
                        throw new Error(responseJson.message ? responseJson.message : "Oops, something went wrong!");
                    }

                    setTotalAmountOfRecords(responseJson.totalElements);
                    setTotlalPages(responseJson.totalPages);

                    const responseRecordsContentArray = responseJson.content;

                    const loadedHistoryRecords: HistoryRecordModel[] = [];

                    for (const key in responseRecordsContentArray) {

                        loadedHistoryRecords.push(responseRecordsContentArray[key]);
                    }

                    setHistoryRecords(loadedHistoryRecords);
                    setIsLoading(false);
                };

                setIsLoading(false);
            };

            fetchUserHistoryRecords().catch(

                (error: any) => {

                    setIsLoading(false);
                    setHttpError(error.message);
                }
            )

        }, [currentPage]

    );

}