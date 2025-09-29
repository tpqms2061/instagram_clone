import api from "./api";

// backend 컨트롤러에 설정한 것을 여기에 api 로 설정하는 것
const likeService = {
  toggleLike: async (postId) => {
    const response = await api.post(`/api/likes/${postId}`);
    return response.data;
  },

  countLike: async (postId) => {
    const response = await api.get(`/api/likes/count/${postId}`);
    return response.data;
  },

  isLiked: async (postId) => {
    const response = await api.get(`/api/likes/is-liked/${postId}`);
    return response.data;
  },
};

export default likeService;
