// Navbar links 

export const navLinks = [

    {id: 1, href: "/home",        title: "Home",         authRequired: false, adminOnly: false },
    {id: 2, href: "/search",      title: "Search Books", authRequired: false, adminOnly: false },
    {id: 3, href: "/shelf",       title: "Shelf",        authRequired: true,  adminOnly: false },
    {id: 4, href: "/discussions", title: "Discussions",  authRequired: true,  adminOnly: false },
    {id: 5, href: "/fees",        title: "Pay Fees",     authRequired: true,  adminOnly: false },
    {id: 6, href: "/admin",       title: "Admin",        authRequired: true,  adminOnly: true  }
    
];


// Quotes on top of each page 

export const quotes = [

    {id: 1, text: "A reader lives a thousand lives before he dies, said Jojen. The man who never reads lives only one.", author: "George R.R. Martin"},
    {id: 2, text: "Reality doesn't always give us the life that we desire, but we can always find what we desire between the pages of books.", author: "Adelise M. Cullens"},
    {id: 3, text: "When I have a house of my own, I shall be miserable if I have not an excellent library.", author: "Jane Austen"},
    {id: 4, text: "Maybe this is why we read, and why in moments of darkness we return to books: to find words for what we already know.", author: "Alberto Manguel"},
    {id: 5, text: "When I look back, I am so impressed again with the life-giving power of literature.", author: "Maya Angelou"},
    {id: 6, text: "I believe there is power in words, power in asserting our existence, our experience, our lives, through words.", author: "Jesmyn Ward"},
    {id: 7, text: "I love the sound of the pages flicking against my fingers. Print against fingerprints. Books make people quiet, yet they are so loud.", author: "Nnedi Okorafor"},
    {id: 8, text: "Reading is an act of civilization; it's one of the greatest acts of civilization because it takes the free raw material of the mind and builds castles of possibilities.", author: "Ben Okri"},
    {id: 9, text: "The more that you read, the more things you will know. The more that you learn, the more places you'll go.", author: "Dr. Seuss"},
    {id: 10, text: "You can get lost in any library, no matter the size. But the more lost you are, the more things you'll find.", author: "Millie Florence"}

];


// Rating values for review card 

export const ratings = [

    {id: 1, value: 0.5, name: "0.5 star"},
    {id: 2, value: 1.0, name: "1 star"},
    {id: 3, value: 1.5, name: "1.5 stars"},
    {id: 4, value: 2.0, name: "2 stars"},
    {id: 5, value: 2.5, name: "2.5 stars"},
    {id: 6, value: 3.0, name: "3 stars"},
    {id: 7, value: 3.5, name: "3.5 stars"},
    {id: 8, value: 4.0, name: "4 stars"},
    {id: 9, value: 4.5, name: "4.5 stars"},
    {id: 10, value: 5.0, name: "5 stars"}

];