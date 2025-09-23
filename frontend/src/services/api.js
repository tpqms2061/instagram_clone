import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
    //백엔드랑 통신할떈 json으로 한다고 명시 한것
  },
});

export default api;
