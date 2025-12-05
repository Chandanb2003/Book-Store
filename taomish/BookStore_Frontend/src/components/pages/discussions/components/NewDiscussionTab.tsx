import { NewDiscussionForm } from "./NewDiscussionForm";

export const NewDiscussionTab = () => {

    return (

        <div className="flex max-lg:flex-col gap-10 items-center justify-start">

            <div className="flex-1 flex flex-col items-center gap-5 text-center px-5 max-w-lg">

                <p className="text-3xl max-lg:text-2xl font-semibold leading-snug">
                    What can we help you with?
                </p>

                <div className="text-xl max-lg:text-lg font-light">

                    If you feel like our service is insufficient,
                    or if you have any suggestion on possible improvements,
                    do not hesitate to write about it!

                </div>

                <div className="text-xl max-lg:text-lg font-light">

                    We strive to make our stock a good fit for everyone. 
                    If you have trouble finding something, feel free to 
                    contact us by sending our administration a personal message!

                </div>

                <div className="text-xl max-lg:text-lg font-light">

                    Your message is private, it will only be visible to you and our administration team.

                </div>

            </div>

            <NewDiscussionForm />

        </div>

    )

}