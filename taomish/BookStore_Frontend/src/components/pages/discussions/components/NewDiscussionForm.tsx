import { useState } from "react";
import { useSubmitDiscussion } from "../../../../utils/api_fetchers/discussion_controller/useSubmitDiscussion";
import { DiscussionModel } from "../../../../models/DiscussionModel";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { FormLoader } from "../../../commons/form_loader/FormLoader";
import { FieldErrors } from "../../../commons/field_errors/FieldErrors";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

export const NewDiscussionForm = () => {

    const { authentication } = useAuthenticationContext();

    const [discussionModel, setDiscussionModel] = useState<DiscussionModel>({ title: "", question: "" });
    const [isLoading, setIsLoading] = useState(false);
    const [httpError, setHttpError] = useState<string | null>(null);
    const [displaySuccess, setDisplaySuccess] = useState(false);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => {

        setDiscussionModel({ ...discussionModel, [event.target.name]: event.target.value });
    };

    const handleSubmitDiscussion = () => {
        
        useSubmitDiscussion(authentication, discussionModel, setDiscussionModel, setIsLoading, setHttpError, setDisplaySuccess);
    }

    return (

        <div className="custom-form flex-1">

            {displaySuccess && 
                
                <div className="text-lg font-semibold bg-green-200 rounded-md px-5 py-1">
                    Your message is sent successfully!
                </div>
            
            }

            <p className="text-center text-3xl font-semibold">Open discussion</p>

            <FormLoader isLoading={isLoading} />

            {(httpError && !httpError.startsWith("Some")) && <HttpErrorMessage httpError={httpError} />}

            <form className="flex flex-col gap-5 w-full">

                <div className="flex flex-col gap-1">

                    <FieldErrors fieldName="title" httpError={httpError} />
                    <input type="text" name="title" value={discussionModel.title} onChange={handleChange} placeholder="Discussion title" className="input shadow-md"/>
                
                </div>

                <div className="flex flex-col gap-1">

                    <FieldErrors fieldName="question" httpError={httpError} />
                    <textarea rows={3} name="question" value={discussionModel.question} onChange={handleChange} placeholder="Your question / suggestion here..." className="input shadow-md"/>

                </div>

            </form>

            <button className="custom-btn-2" onClick={handleSubmitDiscussion}>Submit discussion</button>

        </div>

    )

}