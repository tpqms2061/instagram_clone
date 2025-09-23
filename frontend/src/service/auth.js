import api from "./api";

export const authService = {
  async login(userData) {
    const response = await api.post("/api/auth/login", userData);
    const { access_token, refresh_token, user } = response.data;

    localStorage.setItem("accessToken", access_token);
    localStorage.setItem("refreshToken", refresh_token);
    localStorage.setItem("user", JSON.stringify(user));
  },

  async register(userData) {
    const response = await api.post("/api/auth/register", userData);
    const { access_token, refresh_token, user } = response.data;

    localStorage.setItem("acceessToken", access_token);
    localStorage.setItem("refreshToken", refresh_token);
    localStorage.setItem("user", JSON.stringify(user));

    return response.data;
  },
};
