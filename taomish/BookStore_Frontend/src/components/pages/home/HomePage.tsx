import { Quote } from "../../commons/quote/Quote"
import { BeginJourneyHero } from "./components/BeginJourneyHero"
import { BookStoreServices } from "./components/BookStoreServices"
import { Carousel } from "./components/Carousel"
import { FavouriteGenresHero } from "./components/FavouriteGenresHero"
import { GrowingCollectionHero } from "./components/GrowingCollectionHero"

export const HomePage = () => {

    return (

        <div className="page-container lg:px-5">

            <Quote quoteId={0} />

            <BeginJourneyHero />

            <Carousel />

            <div>

                <GrowingCollectionHero />
                
                <FavouriteGenresHero />          

            </div>

            <BookStoreServices />

        </div>

    )

}