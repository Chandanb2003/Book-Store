type HttpErrorMessageProps = {
    httpError: string | null
}

export const HttpErrorMessage = ({ httpError }: HttpErrorMessageProps) => {

    return (

        <div className="error-message">
            
            {httpError}
            
        </div>

    )

}