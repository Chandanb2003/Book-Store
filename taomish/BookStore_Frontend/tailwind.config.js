/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        nunito: ['Nunito Sans', 'sans-serif'],
      },
      boxShadow: {
        'custom': '0 0 10px rgba(0, 0, 0, 0.3)',
        'custom-2': '0 0 20px rgba(0, 0, 0, 0.2)',
        'custom-3': '0 0 10px rgba(0, 0, 0, 0.2)',
      },
      backgroundImage: {
        'home-hero-1': "url('assets/images/home-hero-bg-1.jpg')",
        'home-hero-2': "url('assets/images/home-hero-bg-2.jpg')",
        'home-hero-3': "url('assets/images/home-hero-bg-3.jpg')",
        'home-hero-4': "url('assets/images/home-hero-bg-4.jpg')"
      },
      outlineColor: {
        'custom-outline': "rgba(17, 94, 89, 0.3)"
      },
      keyframes: {
        'open-menu': {
          '0%': { transform: 'translateX(100%)', background: 'transparent', opacity: 0 },
          '10%': { opacity: 0 },
          '100%': { transform: 'translateX(0%)', background: 'rgb(204, 251, 241)', opacity: 1} 
        },

        'rotate': {
          '0%': { transform: 'rotate(0deg) scale(1)' },
          '100%': { transform: 'rotate(360deg) scale(2)' }
        }
      },
      animation: {
        'open-menu': 'open-menu 0.5s ease-in-out forwards',
        'rotate': 'rotate 5s linear infinite alternate '
      }
    },
  },
  plugins: [],
}