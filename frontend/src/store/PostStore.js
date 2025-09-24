import { create } from "zustand";
import postService from "../services/post";

const usePostStore = create((set) => ({
  posts: [],
  loading: false,
  error: null,

  createPost: async (postData) => {
    set({ loading: true, error: null });
    try {
      const newPost = await postService.createPost(postData);
      set((state) => ({
        posts: [newPost, ...state.posts],
        loading: false,
      }));
    } catch (err) {
      set({
        error: err.response?.data.message || "Failed to create post",
        loading: false,
      });
      throw err;
    }
  },

  fetchPosts: async (page = 0, refresh = false) => {
    set({ loading: true, error: null });
    try {
      const response = await postService.getAllPosts(page);

      set({
        posts: response.content,
        loading: false,
      });
    } catch (err) {
      set({
        error: err.response?.data.message || "Failed to fetch posts",
        loading: false,
      });
      throw err;
    }
  },
  //삭제한 결과를 주지않고 get요청을 태울필요없음 어떤게 삭제된줄 알기때문
  deletePost: async (postId) => {
    set({ loading: true, error: null });
    try {
      await postService.deletePost(postId);
      set((state) => ({
        posts: state.posts.filter((p) => p.id !== postId),
        //userPosts,
        // currentPost
        loading: false,
      }));
    } catch (err) {
      set({
        error: err.response?.data.message || "Failed to delete posts",
        loading: false,
      });
      throw err;
    }
  },

  updatePost: async (postId, postData) => {
    set({ loading: true, error: null });
    try {
      const updatedPost = await postService.updatePost(postId, postData);
      set((state) => ({
        posts: state.posts.map((p) => (p.id === postId ? updatedPost : p)),
        loading: false,
      })); //바뀐 결과에 대한 내용을 기존에서 변경 map함수로 나머지는 그대로 사용 업데이트 된 애들만 받아오도록
      return updatedPost;
    } catch (err) {
      set({
        error: err.response?.data.message || "Failed to update posts",
        loading: false,
      });
      throw err;
    }
  },
}));

export default usePostStore;
