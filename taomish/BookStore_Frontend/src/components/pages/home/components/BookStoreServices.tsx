import { Link } from "react-router-dom"
import { useAuthenticationContext } from "../../../../authentication/authenticationContext";

export const BookStoreServices = () => {

    const { authentication } = useAuthenticationContext();

    return (

        <div className="bg-home-hero-4 bg-cover max-lg:bg-right w-full">
            
            <div className="bg-white bg-opacity-60 w-full flex items-center justify-end p-5">

                <div className="flex flex-col items-center lg:gap-16 gap-10 lg:w-1/2 text-center">

                    <p className="text-5xl max-lg:text-3xl font-semibold leading-snug w-4/6">
                        Missing something?
                    </p>

                    <div className="text-xl max-lg:text-lg font-light w-4/6">

                        We strive to make our stock a good fit for everyone. 
                        If you have trouble finding something, feel free to 
                        contact us by sending our administration a personal message!

                    </div>

                    <Link to={authentication.isAuthenticated ? "/discussions" : "/login"} type="button" className="custom-btn-2">
                        {authentication.isAuthenticated ? "Open discussion" : "Sign in to open discussion"}
                    </Link>

                </div>

            </div>

        </div>

    )

}