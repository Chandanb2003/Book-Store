type PaginatedItemsCountProps = {
    itemsName: string,
    totalAmountOfItems: number,
    resultRange: { start: number; end: number; }
}

export const PaginatedItemsCount = ({ itemsName, totalAmountOfItems, resultRange }: PaginatedItemsCountProps) => {

    return (

        <div className="flex gap-4 items-center justify-center text-xl">

            {itemsName}: 

            <p className="sm:text-3xl max-sm:text-xl text-teal-600">
                {resultRange.start} - {totalAmountOfItems <= 5 ? totalAmountOfItems : resultRange.end}
            </p> 

            out of 

            <p className="sm:text-3xl max-sm:text-xl text-teal-600">{totalAmountOfItems}</p>

        </div>

    )

}