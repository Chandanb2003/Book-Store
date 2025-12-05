// ------------------------------------------------------------------------------------------------------------
// BASE API URL COMMON PART
// ------------------------------------------------------------------------------------------------------------
const base_url = `${import.meta.env.VITE_BACKEND_BASE_URL}`;


// ------------------------------------------------------------------------------------------------------------
// API CONTROLLERS ENTRY URLS
// ------------------------------------------------------------------------------------------------------------
const admin_controller_entry          = `${base_url}${import.meta.env.VITE_ADMIN_CONTROLLER_URL_PART}`;
const book_controller_entry           = `${base_url}${import.meta.env.VITE_BOOK_CONTROLLER_URL_PART}`;
const checkout_controller_entry       = `${base_url}${import.meta.env.VITE_CHECKOUT_CONTROLLER_URL_PART}`;
const discussion_controller_entry     = `${base_url}${import.meta.env.VITE_DISCUSSION_CONTROLLER_URL_PART}`;
const genre_controller_entry          = `${base_url}${import.meta.env.VITE_GENRE_CONTROLLER_URL_PART}`;
const history_record_controller_entry = `${base_url}${import.meta.env.VITE_HISTORY_RECORD_CONTROLLER_URL_PART}`;
const payment_controller_entry        = `${base_url}${import.meta.env.VITE_PAYMENT_CONTROLLER_URL_PART}`;
const review_controller_entry         = `${base_url}${import.meta.env.VITE_REVIEW_CONTROLLER_URL_PART}`;
const authentication_controller_entry = `${base_url}${import.meta.env.VITE_AUTHENTICATION_CONTROLLER_URL_PART}`;


// ------------------------------------------------------------------------------------------------------------
// API CONTROLLERS SECURE URLS COMMON PART
// ------------------------------------------------------------------------------------------------------------
const secure_url_part = `${import.meta.env.VITE_SECURE_URL_PART}`


// ------------------------------------------------------------------------------------------------------------
// API ADMIN CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const add_book_endpoint             = "/add-book";
const increase_quantity_endpoint    = "/increase-quantity";
const decrease_quantity_endpoint    = "/decrease-quantity";
const delete_book_endpoint          = "/delete-book";
const get_open_discussions_endpoint = "/open-discussions";
const close_discussion_endpoint     = "/close-discussion";


// ------------------------------------------------------------------------------------------------------------
// API BOOK CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const search_by_title_endpoint = "/search/by-title";
const search_by_genre_endpoint = "/search/by-genre";
const is_checked_out_endpoint  = "/is-checked-out";
const checkout_book_endpoint   = "/checkout";
const renew_checkout_endpoint  = "/renew-checkout";
const return_book_endpoint     = "/return";
const is_reviewed_endpoint     = "/is-reviewed";
const review_book_endpoint     = "/review";


// ------------------------------------------------------------------------------------------------------------
// API CHECKOUT CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const current_loans_count_endpoint = "/current-loans-count";
const current_checkouts_endpoint   = "/current-checkouts";


// ------------------------------------------------------------------------------------------------------------
// API DISCUSSION CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const add_discussion_endpoint = "/add-discussion";


// ------------------------------------------------------------------------------------------------------------
// API PAYMENT CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const create_payment_intent_endpoint = "/payment-intent";
const complete_payment_endpoint      = "/payment-complete";


// ------------------------------------------------------------------------------------------------------------
// API REVIEW CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const get_avg_rating_endpoint = "/average-rating";


// ------------------------------------------------------------------------------------------------------------
// API AUTHENTICATION CONTROLLER ENDPOINTS
// ------------------------------------------------------------------------------------------------------------
const register_user_endpoint     = "/register";
const authenticate_user_endpoint = "/authenticate";


// ------------------------------------------------------------------------------------------------------------
// API CONTROLLERS ENDPOINTS LISTS
// ------------------------------------------------------------------------------------------------------------

export const admin_controller_endpoints = {

    add_book:             { method: "POST",   url: new URL(`${admin_controller_entry}${secure_url_part}${add_book_endpoint}`) },
    increase_quantity:    { method: "PATCH",  url: new URL(`${admin_controller_entry}${secure_url_part}${increase_quantity_endpoint}`) },
    decrease_quantity:    { method: "PATCH",  url: new URL(`${admin_controller_entry}${secure_url_part}${decrease_quantity_endpoint}`) },
    delete_book:          { method: "DELETE", url: new URL(`${admin_controller_entry}${secure_url_part}${delete_book_endpoint}`) },
    get_open_discussions: { method: "GET",    url: new URL(`${admin_controller_entry}${secure_url_part}${get_open_discussions_endpoint}`) },
    close_discussion:     { method: "PATCH",  url: new URL(`${admin_controller_entry}${secure_url_part}${close_discussion_endpoint}`) }

};

export const book_controller_endpoints = {

    find_all_books:  { method: "GET",  url: new URL(`${book_controller_entry}`) },
    find_by_id:      { method: "GET",  url: new URL(`${book_controller_entry}`) },
    search_by_title: { method: "GET",  url: new URL(`${book_controller_entry}${search_by_title_endpoint}`) },
    search_by_genre: { method: "GET",  url: new URL(`${book_controller_entry}${search_by_genre_endpoint}`) },
    is_checked_out:  { method: "GET",  url: new URL(`${book_controller_entry}${secure_url_part}${is_checked_out_endpoint}`) },
    checkout_book:   { method: "PUT",  url: new URL(`${book_controller_entry}${secure_url_part}${checkout_book_endpoint}`) },
    renew_checkout:  { method: "PUT",  url: new URL(`${book_controller_entry}${secure_url_part}${renew_checkout_endpoint}`) },
    return_book:     { method: "PUT",  url: new URL(`${book_controller_entry}${secure_url_part}${return_book_endpoint}`) },
    is_reviewed:     { method: "GET",  url: new URL(`${book_controller_entry}${secure_url_part}${is_reviewed_endpoint}`) },
    review_book:     { method: "POST", url: new URL(`${book_controller_entry}${secure_url_part}${review_book_endpoint}`) }

};

export const checkout_controller_endpoints = {

    current_loans_count: { method: "GET", url: new URL(`${checkout_controller_entry}${secure_url_part}${current_loans_count_endpoint}`) },
    current_checkouts:   { method: "GET", url: new URL(`${checkout_controller_entry}${secure_url_part}${current_checkouts_endpoint}`) }

};

export const discussion_controller_endpoints = {

    find_all_discussions: { method: "GET",  url: new URL(`${discussion_controller_entry}${secure_url_part}`) },
    add_discussion:       { method: "POST", url: new URL(`${discussion_controller_entry}${secure_url_part}${add_discussion_endpoint}`) }

};

export const genre_controller_endpoints = {

    find_all_genres: { method: "GET", url: new URL(`${genre_controller_entry}`) }

};

export const history_record_controller_endpoints = {

    find_all_history_records: { method: "GET", url: new URL(`${history_record_controller_entry}${secure_url_part}`) }

};

export const payment_controller_endpoints = {

    get_payment_info:      { method: "GET",  url: new URL(`${payment_controller_entry}${secure_url_part}`) },
    create_payment_intent: { method: "POST", url: new URL(`${payment_controller_entry}${secure_url_part}${create_payment_intent_endpoint}`) },
    complete_payment:      { method: "PUT",  url: new URL(`${payment_controller_entry}${secure_url_part}${complete_payment_endpoint}`) }

};

export const review_controller_endpoints = {

    find_all_reviews: { method: "GET", url: new URL(`${review_controller_entry}`) },
    get_avg_rating:   { method: "GET", url: new URL(`${review_controller_entry}${get_avg_rating_endpoint}`) }

};

export const authentication_controller_endpoints = {

    register_user:     { method: "POST",   url: new URL(`${authentication_controller_entry}${register_user_endpoint}`) },
    authenticate_user: { method: "POST",   url: new URL(`${authentication_controller_entry}${authenticate_user_endpoint}`) }

};