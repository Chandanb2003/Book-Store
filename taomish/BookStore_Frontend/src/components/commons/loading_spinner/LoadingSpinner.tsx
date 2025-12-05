export const LoadingSpinner = () => {

    return (

        <div className="relative flex items-center justify-center p-10">

            <div className=" w-20 h-20 rounded-full border-4 border-dashed border-teal-600 animate-rotate" />

            <div className=" absolute text-sm font-semibold">
                Loading
            </div>

        </div>

    )

}