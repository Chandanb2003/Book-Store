import { Navigate } from "react-router-dom";
import { useAuthenticationContext } from "../../../authentication/authenticationContext";
import { Quote } from "../../commons/quote/Quote"
import { useState } from "react";
import { AddBookTab } from "./components/AddBookTab";
import { QuantitiesTab } from "./components/QuantitiesTab";
import { DiscussionsTab } from "./components/DiscussionsTab";

export const AdminPage = () => {

    const { authentication } = useAuthenticationContext();

    if (!authentication.isAuthenticated || authentication.authority !== "ROLE_ADMIN") return <Navigate to={"/"} />

    const [isAddBookTabSelected, setIsAddBookTabSelected] = useState(true);
    const [isQuantitiesTabSelected, setIsQuantitiesTabSelected] = useState(false);
    const [isDiscussionsTabSelected, setIsDiscussionsTabSelected] = useState(false);

    const handleAddBookTabClick = () => {

        setIsAddBookTabSelected(true);
        setIsQuantitiesTabSelected(false);
        setIsDiscussionsTabSelected(false);
    };

    const handleQuantitiesTabClick = () => {

        setIsAddBookTabSelected(false);
        setIsQuantitiesTabSelected(true);
        setIsDiscussionsTabSelected(false);
    };

    const handleDiscussionsTabClick = () => {

        setIsAddBookTabSelected(false);
        setIsQuantitiesTabSelected(false);
        setIsDiscussionsTabSelected(true);
    };

    return (

        <div className="page-container">

            <Quote quoteId={5} />

            <div className="w-full p-5 flex max-lg:flex-col gap-12 relative">

                <div className="flex lg:flex-col lg:gap-5 max-lg:gap-1 max-lg:w-full">

                    <button className={isAddBookTabSelected ? "nav-link-active-2" : "nav-link-2"} onClick={handleAddBookTabClick}>
                        Add Book
                    </button>

                    <button className={isQuantitiesTabSelected ? "nav-link-active-2" : "nav-link-2"} onClick={handleQuantitiesTabClick}>
                        Quantities
                    </button>

                    <button className={isDiscussionsTabSelected ? "nav-link-active-2" : "nav-link-2"} onClick={handleDiscussionsTabClick}>
                        Discussions
                    </button>

                </div>

                <div className="divider-1 lg:left-40" />

                <div className="w-full">

                    { isAddBookTabSelected && <AddBookTab /> }
                    { isQuantitiesTabSelected && <QuantitiesTab /> }
                    { isDiscussionsTabSelected && <DiscussionsTab /> }

                </div>

            </div>

        </div>

    )
}