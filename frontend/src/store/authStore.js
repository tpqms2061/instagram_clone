import { create } from "zustand";
import { authService } from "../service/auth";

const useAuthStore = create((set) => ({
  user: "",
  isAuthenticated: "",
  loading: false,
  error: null,

  register: async (userData) => {
    set({ loading: true, error: null });
    try {
      //서버로부터 데이터 요청 & 응답
      await authService.register(userData);
      //set 에 반영
    } catch (err) {
      set({
        loading: false,
        error: err.response?.data?.message || "Registration failed",
      });
      throw err;
    }
  },
}));

export default useAuthStore;
