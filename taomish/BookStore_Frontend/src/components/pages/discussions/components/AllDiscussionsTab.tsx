import { useState } from "react";
import { DiscussionModel } from "../../../../models/DiscussionModel";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { useFetchDiscussions } from "../../../../utils/api_fetchers/discussion_controller/useFetchDiscussions";
import { LoadingSpinner } from "../../../commons/loading_spinner/LoadingSpinner";
import { Pagination } from "../../../commons/pagination/Pagination";
import { AllDiscussionsTabDiscussionCard } from "./AllDiscussionsTabDiscussionCard";
import { PaginatedItemsCount } from "../../../commons/pagination/PaginatedItemsCount";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

type AllDiscussionsTabProps = {
    setIsAllDiscussionsTabSelected: React.Dispatch<React.SetStateAction<boolean>>
}

export const AllDiscussionsTab = ({ setIsAllDiscussionsTabSelected }: AllDiscussionsTabProps) => {

    const { authentication } = useAuthenticationContext();

    const [discussions, setDiscussions] = useState<DiscussionModel[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [httpError, setHttpError] = useState<string | null>(null);

    const [currentPage, setCurrentPage] = useState(1);
    const [totalAmountOfDiscussions, setTotalAmountOfDiscussions] = useState(0);
    const [totalPages, setTotlalPages] = useState(0);
    const [resultRange, setResultRange] = useState({start: 1, end: 5});

    useFetchDiscussions(authentication, setDiscussions, setTotalAmountOfDiscussions, setTotlalPages, setIsLoading, setHttpError, currentPage);

    return (

        <div className="flex flex-col gap-10 lg:items-start">

            {isLoading ? <LoadingSpinner /> : 
            
                <>

                    {httpError ? <HttpErrorMessage httpError={httpError} /> :
                        
                        <>

                            {discussions.length === 0 ? 

                                <div className="flex flex-col gap-10 lg:items-start max-lg:items-center">

                                    <p className="text-2xl font-semibold">No discussions yet.</p>

                                    <button className="custom-btn-2" onClick={() => setIsAllDiscussionsTabSelected(false)}>
                                        Open discussion
                                    </button>

                                </div>

                                :

                                <>

                                    <PaginatedItemsCount itemsName={"Your discussions"} totalAmountOfItems={totalAmountOfDiscussions} resultRange={resultRange} />

                                    {discussions.map(
                                            
                                        discussion => <AllDiscussionsTabDiscussionCard key={discussion.id} discussion={discussion} />

                                    )}

                                    <Pagination currentPage={currentPage} totalPages={totalPages} totalAmountOfItems={totalAmountOfDiscussions} 
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