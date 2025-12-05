import gitLogo from "../../../assets/icons/github-logo-teal.svg";
import linkedInLogo from "../../../assets/icons/linkedin-logo-teal.svg";
import bookStoreLogo from "../../../assets/images/BookStore_Logo.png";

export const Footer = () => {

    return (

        <footer className="w-full bg-teal-800 relative lg:py-10 max-lg:pt-10 max-lg:pb-20">

            <div className="w-full flex max-lg:flex-col max-lg:gap-10 items-center justify-between lg:px-20">

                <div className="flex w-1/3 flex-col gap-10 text-center">

                    <p className="text-teal-100 font-bold text-3xl">
                        Contacts
                    </p>

                    <div className="flex flex-col gap-1 text-lg font-light items-center">

                        <p className="text-teal-100">bookstore.services@gmail.com</p>
                        <p className="text-teal-100">bookstore.administration@gmail.com</p>

                    </div>

                </div>

                <div className="max-lg:hidden w-1/3 flex flex-col items-center">

                    <img className="w-40 h-auto pl-5" src={bookStoreLogo} alt="bookStoreLogo" />

                    <p className="text-teal-100 text-center">BookStore project</p>
                    <p className="text-teal-100 text-center">Developed by IMDev</p>

                </div>

                <div className="flex w-1/3 flex-col gap-10 text-center">

                    <p className="text-teal-100 font-bold text-3xl">
                        Socials
                    </p>

                    <div className="flex gap-5 text-lg items-center justify-center">

                        <a href={import.meta.env.VITE_GITHUB_LINK} target="blanc">

                            <img className="w-[60px] h-[60px]" src={gitLogo} alt="gitLogo" />

                        </a>

                        <a href={import.meta.env.VITE_LINKEDIN_LINK} target="blanc">

                            <img className="w-[60px] h-[60px]"  src={linkedInLogo} alt="linkedInLogo" />

                        </a>

                    </div>

                </div>

            </div>
        
            <div className="absolute bottom-0 left-0 right-0 lg:hidden bg-teal-950 flex gap-10 items-center justify-center lg:px-20 max-lg:px-5 py-1 font-light">

                <p className="text-teal-200">BookStore project</p>
                <p className="text-teal-200">Developed by IMDev</p>

            </div>
        
        </footer>

    )

}