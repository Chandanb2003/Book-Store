type FormLoaderProps = {
    isLoading: boolean
}

export const FormLoader = ({ isLoading }: FormLoaderProps) => {

    return (

        <div className="divider-2 flex items-center justify-center">

            {isLoading && <div className="w-5 h-5 rounded-full border-2 border-dashed border-teal-600 animate-rotate bg-teal-100"/>}

        </div>

    )

}