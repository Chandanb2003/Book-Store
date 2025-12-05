import { useState } from "react";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { HistoryRecordModel } from "../../../../models/HistoryRecordModel";
import { useFetchHistoryRecords } from "../../../../utils/api_fetchers/history_record_controller/useFetchHistoryRecords";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { Link } from "react-router-dom";
import { Pagination } from "../../../commons/pagination/Pagination";
import { HistoryTabRecordCard } from "./HistoryTabRecordCard";
import { PaginatedItemsCount } from "../../../commons/pagination/PaginatedItemsCount";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

export const HistoryTab = () => {

    const { authentication } = useAuthenticationContext();

    const [historyRecords, setHistoryRecords] = useState<HistoryRecordModel[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [httpError, setHttpError] = useState<string | null>(null);

    const [currentPage, setCurrentPage] = useState(1);
    const [totalAmountOfRecords, setTotalAmountOfRecords] = useState(0);
    const [totalPages, setTotlalPages] = useState(0);
    const [resultRange, setResultRange] = useState({start: 1, end: 5});

    useFetchHistoryRecords(authentication, setHistoryRecords, setTotalAmountOfRecords, setTotlalPages, setIsLoading, setHttpError, currentPage);

    return (

        <div className="flex flex-col gap-10 lg:items-start">

            {isLoading ? <LoadingSpinner /> : 
            
                <>

                    {httpError ? <HttpErrorMessage httpError={httpError} /> : 
                        
                        <>

                            {historyRecords.length === 0 ? 

                                <div className="flex flex-col gap-10 lg:items-start max-lg:items-center">

                                    <p className="text-2xl font-semibold">No history records yet.</p>

                                    <Link to={'/search'} className="custom-btn-2 text-center">
                                        Search for books
                                    </Link>

                                </div>

                                :

                                <>

                                    <PaginatedItemsCount itemsName={"History Records"} totalAmountOfItems={totalAmountOfRecords} resultRange={resultRange} />

                                    {historyRecords.map(
                                            
                                        record => <HistoryTabRecordCard key={record.id} record={record} />

                                    )}

                                    <Pagination currentPage={currentPage} totalPages={totalPages} totalAmountOfItems={totalAmountOfRecords} 
                                        setCurrentPage={setCurrentPage} setResultRange={setResultRange} 
                                    />

                                </>

                            }

                        </>

                    }

                </>

            }
            
        </div>

    )

}