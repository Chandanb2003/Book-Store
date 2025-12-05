import { Navigate } from "react-router-dom";
import { Quote } from "../../commons/quote/Quote"
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { useState } from "react";
import { NewDiscussionTab } from "./components/NewDiscussionTab";
import { AllDiscussionsTab } from "./components/AllDiscussionsTab";

export const DiscussionsPage = () => {

    const { authentication } = useAuthenticationContext();

    if (!authentication.isAuthenticated) return <Navigate to={"/"} />

    const [isAllDiscussionsTabSelected, setIsAllDiscussionsTabSelected] = useState(false);

    return (

        <div className="page-container">

            <Quote quoteId={3} />

            <div className="w-full p-5 flex max-lg:flex-col gap-12 relative">

                <div className="flex lg:flex-col gap-5">

                    <button className={isAllDiscussionsTabSelected ? "nav-link-2" : "nav-link-active-2"} onClick={() => setIsAllDiscussionsTabSelected(false)}>
                        New discussion
                    </button>

                    <button className={isAllDiscussionsTabSelected ? "nav-link-active-2" : "nav-link-2"} onClick={() => setIsAllDiscussionsTabSelected(true)}>
                        All discussions
                    </button>

                </div>

                <div className="divider-1 lg:left-48" />

                <div className="w-full">

                    { isAllDiscussionsTabSelected ? <AllDiscussionsTab setIsAllDiscussionsTabSelected={setIsAllDiscussionsTabSelected} /> : <NewDiscussionTab /> }

                </div>

            </div>

        </div>

    )

}