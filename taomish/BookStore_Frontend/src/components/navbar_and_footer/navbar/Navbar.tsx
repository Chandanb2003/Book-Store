import { Link, NavLink } from "react-router-dom";
import { navLinks } from "../../../constants/constants";
import bookStoreLogo from "../../../assets/images/BookStore_Logo.png";
import hamburgerMenuLogo from "../../../assets/icons/hamburger-menu.svg";
import xMenuLogo from "../../../assets/icons/x-menu.svg";
import { useState } from "react";
import { useAuthenticationContext } from "../../../authentication/authenticationContext";

export const Navbar = () => {

    const { authentication, logout } = useAuthenticationContext();

    const [hamburgerMenuClicked, setHamburgerMenuClicked] = useState(false);

    return (

        <header className="max-lg:bg-teal-200 bg-white z-10 w-full h-[70px] fixed top-0">

            <nav className="flex justify-between items-center max-w-[1440px] mx-auto px-6 gap-5">

                <NavLink to="/" className="flex items-center gap-3">

                    <img src={bookStoreLogo} alt="logo" width={70} height={70} />
                    <p className="text-2xl whitespace-nowrap">Book Store</p>

                </NavLink>

                <div className={`${!hamburgerMenuClicked && "max-lg:hidden"} nav-menu max-lg:origin-right max-lg:animate-open-menu`}>

                    {navLinks.map(
                        
                        (link) => (
                            
                            link.authRequired ? authentication.isAuthenticated && (

                                link.adminOnly ? authentication.authority === "ROLE_ADMIN" &&

                                <NavLink key={link.id} className={({ isActive }) => (isActive ? "nav-link-active" : "nav-link")} to={link.href} onClick={() => setHamburgerMenuClicked(false)}>

                                    {link.title}

                                </NavLink>

                                :

                                <NavLink key={link.id} className={({ isActive }) => (isActive ? "nav-link-active" : "nav-link")} to={link.href} onClick={() => setHamburgerMenuClicked(false)}>

                                    {link.title}

                                </NavLink>

                            )

                            :

                            <NavLink key={link.id} className={({ isActive }) => (isActive ? "nav-link-active" : "nav-link")} to={link.href} onClick={() => setHamburgerMenuClicked(false)}>

                                {link.title}

                            </NavLink>
                            
                        )
                        
                    )}

                    {!authentication.isAuthenticated ?

                        <Link to={"/login"} className="custom-btn-3 lg:hidden mt-4" onClick={() => setHamburgerMenuClicked(false)}>Sign In</Link>

                        :

                        <button className="custom-btn-3 lg:hidden mt-4" onClick={logout}>Log out</button>
                    }

                </div>

                {!authentication.isAuthenticated ?

                    <Link to={"/login"} className="custom-btn-1 max-lg:hidden">Sign In</Link>

                    :

                    <button className="custom-btn-1 max-lg:hidden" onClick={logout}>Log out</button>
                }

                <button className="p-1 hidden max-lg:block" onClick={() => setHamburgerMenuClicked(!hamburgerMenuClicked)}>
                    
                    <img src={hamburgerMenuClicked ? xMenuLogo : hamburgerMenuLogo} alt="menu" width={40} height={40} />

                </button>

            </nav>
            
        </header>

    )
}