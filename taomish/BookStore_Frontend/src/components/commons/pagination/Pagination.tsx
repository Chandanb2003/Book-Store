type PaginationProps = {
    currentPage: number,
    totalPages: number,
    totalAmountOfItems: number,
    setCurrentPage: React.Dispatch<React.SetStateAction<number>>,
    setResultRange: React.Dispatch<React.SetStateAction<{ start: number; end: number; }>>
}

export const Pagination = ({ currentPage, totalPages, totalAmountOfItems, setCurrentPage, setResultRange }: PaginationProps) => {

    const pageNumbers = [];

    if (totalPages <= 5) {

        for (let i = 1; i <= totalPages; i++) pageNumbers.push(i);

    } else {

        if (currentPage < 3) {

            for (let i = 1; i <= 5; i++) pageNumbers.push(i);
        }

        if (currentPage >= 3 && currentPage + 2 <= totalPages) {

            for (let i = currentPage - 2; i <= currentPage + 2; i++) pageNumbers.push(i);
        }

        if (currentPage > totalPages - 2) {

            for (let i = totalPages - 4; i <= totalPages; i++) pageNumbers.push(i);
        }

    }
    
    const handleFirstClick = () => {

        window.scrollTo(0, 0);

        if (currentPage !== 1) {

            setResultRange({ start: 1, end: 5 });
            setCurrentPage(1);
        }
    }
    
    const handlePageButtonClick = (p: number) => {

        window.scrollTo(0, 0);

        if (p === 1) handleFirstClick();
        else if (p === totalPages) handleLastClick();
        else {
            setResultRange({ start: ((p - 1) * 5) + 1, end: (p * 5) });
            setCurrentPage(p);
        }
    };

    const handleLastClick = () => {

        window.scrollTo(0, 0);

        if (currentPage !== totalPages) {

            setResultRange({ start: totalAmountOfItems - (totalAmountOfItems - ((totalPages * 5 - 4))), end: totalAmountOfItems });
            setCurrentPage(totalPages);
        }
    }

    return (

        <>

            {totalPages > 0 &&

                <div className="flex gap-4 max-md:gap-2 p-5">
                    
                    <button className="custom-btn-1 pagination-btn-small" onClick={handleFirstClick}>First</button>

                    {pageNumbers.map(p =>

                        <button className={currentPage === p ? "custom-btn-2 text-xl font-bold pagination-btn-small" : "custom-btn-1 pagination-btn-small"} onClick={() => handlePageButtonClick(p)} key={p}>
                            {p}
                        </button>

                    )}

                    <button className="custom-btn-1 pagination-btn-small" onClick={handleLastClick}>Last</button>

                </div>
            }

        </>

    )

}