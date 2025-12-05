import { Navigate } from "react-router-dom";
import { Quote } from "../../commons/quote/Quote"
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { useState } from "react";
import { HistoryTab } from "./components/HistoryTab";
import { CheckoutsTab } from "./components/CheckoutsTab";

export const ShelfPage = () => {

    const { authentication } = useAuthenticationContext();

    if (!authentication.isAuthenticated) return <Navigate to={"/"} />

    const [isHistoryTabSelected, setIsHistoryTabSelected] = useState(false);

    return (

        <div className="page-container">

            <Quote quoteId={2} />

            <div className="w-full p-5 flex max-lg:flex-col gap-12 relative">

                <div className="flex lg:flex-col gap-5">

                    <button className={isHistoryTabSelected ? "nav-link-2" : "nav-link-active-2"} onClick={() => setIsHistoryTabSelected(false)}>
                        Checkouts
                    </button>

                    <button className={isHistoryTabSelected ? "nav-link-active-2" : "nav-link-2"} onClick={() => setIsHistoryTabSelected(true)}>
                        History
                    </button>

                </div>

                <div className="divider-1 lg:left-36" />

                <div className="w-full">

                    { isHistoryTabSelected ? <HistoryTab /> : <CheckoutsTab /> }

                </div>

            </div>

        </div>

    )

}