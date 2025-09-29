//상태 관리

import { create } from "zustand";
import api from "../services/api";
import likeService from "../services/like";
import postService from "../services/post";

const useLikeStore = create((set, get) => ({
  likes: {},
  loading: false,
  error: null,

  toggleLike: async (postId) => {
    set({ loading: true, error: null });

    try {
      const { isLiked, likeCount } = await postService.toggleLike(postId); //직관적으로 변경하기위해서 변경

      set((state) => ({
        like: {
          ...state.likes,
          [postId]: {
            isLiked,
            likeCount,
          },
        },
        loading: false,
      }));

      return { isLiked, likeCount };
    } catch (err) {
      set({
        loading: false,
        error: err.response?.data?.message || "Failed to toggle like",
      });
      throw err;
    }
  },
}));

export default useLikeStore;
