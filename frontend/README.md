# TalentTrade React Frontend

TalentTrade is a peer-to-peer non-monetary skill exchange web application built using **React 19**, **Vite**, **Tailwind CSS**, **Shadcn UI style primitives**, **React Router**, and **Lucide Icons**.

This frontend is designed to interface with the local Spring Boot PostgreSQL backend and supports real-time WebSockets communication (SockJS/STOMP) for chat messaging.

---

## 🛠️ Tech Stack & Dependencies

*   **Framework**: React 19 & Vite
*   **Styling**: Tailwind CSS & custom HSL colors
*   **Icons**: Lucide React
*   **Routing**: React Router DOM (v6)
*   **API Client**: Axios (configured with request/response interceptors to store and inject JWT token credentials automatically)
*   **Forms**: React Hook Form
*   **WebSockets**: SockJS-Client & StompJS
*   **Toasts**: React Toastify

---

## 📂 Directory Layout

```text
frontend/
├── public/
├── src/
│   ├── assets/             # Vector icons & logos
│   ├── components/         # Reusable Tailwind Shadcn UI primitives
│   │   ├── ui/             # Card, Table, Button, Input, Textarea, Badge, Tabs, Dialog, Avatar, Select, Skeleton
│   │   └── Layout.jsx      # Main layout wrappers
│   ├── contexts/           # App level state providers (Auth, Chat, Notifications)
│   ├── hooks/              # Custom context hooks (useAuth, useChat, useNotifications)
│   ├── layouts/            # Dashboard sidebar, drawer layouts
│   ├── pages/              # Responsive page viewports (Landing, Login, Register, Dashboard, Profile, Skills, Matches, Requests, Sessions, Reviews, Notifications, Chat)
│   ├── routes/             # secure route guards & routing map definitions
│   ├── services/           # Axios REST API services & clients
│   ├── styles/             # Tailwind imports & CSS variable mappings
│   ├── utils/              # Class merges
│   ├── App.jsx             # Router wrapping & toast providers binding
│   └── main.jsx            # React root mount script
├── package.json            # Scripts & dependencies
├── vite.config.js          # Port 5173 server & backend API/WebSocket proxies
├── postcss.config.js       # PostCSS compiler config
├── tailwind.config.js      # Custom theme setup
└── index.html              # Core HTML structure and ESM polyfills
```

---

## 🚀 How to Run the Frontend

### Prerequisites
1. Ensure the PostgreSQL database is active.
2. Verify the Spring Boot backend application is running on port `8080`.

### Setup and Development Dev Server

1. Open a terminal in this `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install npm dependencies:
   ```bash
   npm install --legacy-peer-deps
   ```
3. Run the local development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to **`http://localhost:5173/`** to interact with the platform.

### Compiling for Production

To build the static HTML/CSS/JS bundles:
```bash
npm run build
```
The compiled output will be generated inside the `dist/` directory.
