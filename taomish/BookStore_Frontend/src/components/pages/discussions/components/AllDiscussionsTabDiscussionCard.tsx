import { DiscussionModel } from "../../../../models/DiscussionModel";
import avatar from "../../../../assets/icons/avatar.svg";

type AllDiscussionsTabDiscussionCardProps = {
    discussion: DiscussionModel
}

export const AllDiscussionsTabDiscussionCard = ({ discussion }: AllDiscussionsTabDiscussionCardProps) => {

    return (

        <div className="flex flex-col gap-5 p-5 rounded-md shadow-custom-2 w-full">

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

            {discussion.closed ? 

                <>

                    <div className="flex flex-col gap-5 w-full rounded-md shadow-custom-3 p-3">
                        
                        <div className="flex gap-4 items-center">
                                        
                            <img src={avatar} alt="avatar" width={50} height={50} />

                            <div className="w-full max-lg:flex-col flex lg:gap-4 justify-between">
                                
                                <p className="font-bold">Administration team</p>

                                <p className="font-light text-lg">{discussion.adminEmail}</p>

                            </div>

                        </div>

                        <div className="w-full bg-teal-50 border border-teal-800 rounded-md p-3">

                            <p>{`"${discussion.response}"`}</p>

                        </div>

                    </div>

                    <p className="text-green-600 font-semibold text-lg max-lg:text-center">Discussion is closed</p>

                </>

                :

                <div className="text-lg max-lg:text-center">

                    <p className="text-amber-600 font-semibold">Discussion is open</p>

                    <p>Pending response from administration. Thank you for your patience!</p>

                </div>

            }
            
        </div>

    )

}