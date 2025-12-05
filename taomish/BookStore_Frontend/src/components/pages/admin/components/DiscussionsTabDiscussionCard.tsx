import { DiscussionModel } from "../../../../models/DiscussionModel";
import avatar from "../../../../assets/icons/avatar.svg";
import { useState } from "react";
import { useSubmitDiscussionResponse } from "../../../../utils/api_fetchers/admin_controller/useSubmitDiscussionResponse";
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";
import { FormLoader } from "../../../commons/form_loader/FormLoader";
import { HttpErrorMessage } from "../../../commons/http_error_message/HttpErrorMessage";

type DiscussionsTabDiscussionCardProps = {
    discussion: DiscussionModel,
    setIsDiscussionClosed: React.Dispatch<React.SetStateAction<boolean>>
}

export const DiscussionsTabDiscussionCard = ({ discussion, setIsDiscussionClosed }: DiscussionsTabDiscussionCardProps) => {

    const { authentication } = useAuthenticationContext();

    const [discussionModel, setDiscussionModel] = useState<DiscussionModel>({ ...discussion });
    const [isLoading, setIsLoading] = useState(false);
    const [httpError, setHttpError] = useState<string | null>(null);

    const handleChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {

        setDiscussionModel({ ...discussionModel, response: event.target.value });
    }

    const handleSubmitDiscussion = () => {

        useSubmitDiscussionResponse(authentication, discussionModel, setIsLoading, setHttpError, setIsDiscussionClosed);
    }

    return (

        <div className="flex flex-col gap-5 p-5 rounded-md shadow-custom-2 w-full" key={discussion.id}>

            <div className="w-full flex gap-5 max-lg:flex-col justify-between">
            
                <p className="font-semibold lg:text-2xl max-lg:text-xl">
                    
                    <span className="text-teal-600">Case #{discussion.id}: </span>
                    
                    {discussion.title}
                
                </p>
                
                <p className="font-light lg:text-xl max-lg:text-lg">{discussion.personEmail}</p>

            </div>

            <div className="divider-2" />

            <div className="flex flex-col gap-5 w-full rounded-md shadow-custom-3 p-3">
                
                <div className="flex gap-4 items-center text-lg">
                                
                    <img src={avatar} alt="avatar" width={50} height={50} />
                        
                    <p className="font-bold">{discussion.personFirstName} {discussion.personLastName}</p>

                </div>

                <div className="w-full bg-teal-50 border border-teal-800 rounded-md p-3 text-lg">

                    <p>{`"${discussion.question}"`}</p>

                </div>

            </div>

            <div className="flex flex-col gap-5">

                <FormLoader isLoading={isLoading} />

                {httpError && <HttpErrorMessage httpError={httpError} />}

                <textarea rows={3} name="response" value={discussionModel.response ? discussionModel.response : ""} 
                onChange={handleChange} placeholder="Your response here..." className="input shadow-md"/>

            </div>

            <div className="flex max-lg:flex-col gap-5 items-center justify-between">

                <p className="text-amber-600 text-lg font-semibold">Discussion is open</p>

                <button className="custom-btn-2" onClick={handleSubmitDiscussion}>Submit answer</button>

            </div>
            
        </div>

    )

}