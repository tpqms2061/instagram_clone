import { Link } from "react-router-dom";
import Avatar from "../common/Avatar";
import { ko } from "date-fns/locale";
import { formatDistance, formatDistanceToNow } from "date-fns";
import useAuthStore from "../../store/authStore";
import { FiEdit2, FiMoreVertical, FiTrash } from "react-icons/fi";
import { useEffect, useRef, useState } from "react";
import usePostStore from "../../store/PostStore";
import CreatePost from "./CreatePost";
import axios from "axios";

const PostCard = ({ post }) => {
  const { user } = useAuthStore();
  const { deletePost } = usePostStore();

  const isOwner = post.user.id == user.id;
  const [imageUrl, setImageUrl] = useState("");

  const menuRef = useRef(null);

  const [showMenu, setShowMenu] = useState(false);

  const [showUpdatePost, setShowUpdatePost] = useState(false); //모달창을 열고닫고 해야될때 사용하는 useState

  const handleDelete = async () => {
    if (window.confirm("Are you sure you want to delete this post?")) {
      try {
        await deletePost(post.id);
      } catch (err) {
        alert("Failed to delete post. Please try again.");
      } finally {
        setShowMenu(false);
      }
    }
  };

  useEffect(() => {
    const handleClickOutSide = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowMenu(false);
      }
    };
    if (showMenu) {
      document.addEventListener("mousedown", handleClickOutSide);
      return () => {
        document.removeEventListener("mousedown", handleClickOutSide);
      };
    }
  }, [showMenu]);

  //이미지 업로드 하면 보이게 하는 로직
  useEffect(() => {
    const getImage = async () => {
      try {
        const token = localStorage.getItem("accessToken");

        if (!token || token === "undefined" || token === "null") {
          throw new Error(
            "No valid authenication token found. Please login again."
          );
        }

        const response = await axios.get(
          `${import.meta.env.VITE_API_URL}/api/posts/image?url=${
            post.imageUrl
          }`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        setImageUrl(response.data.imageUrl);
      } catch (err) {
        console.error(err);
      }
    };

    if (!post.imageUrl) return;

    getImage();
  }, [post]);

  return (
    <>
      <div className="bg-white rounded-lg shadow-md">
        <div className="flex items-center justify-between p-4">
          <Link
            to={`/profile/${post.user.username}`}
            className="flex items-center space-x-3"
          >
            <Avatar user={post.user} size="medium" />
            <div>
              <p className="font-semibold text-sm">{post.user.username}</p>
            </div>
          </Link>

          {/* 더보기 디자인 */}
          {isOwner && (
            <div className="relative" ref={menuRef}>
              <button
                className="p-2 hover:bg-gray-100 rounded-full transition-colors "
                onClick={() => setShowMenu(!showMenu)}
              >
                <FiMoreVertical size={20} />
              </button>

              {/* //더보기 클릭시 나타나는 효과 */}
              {showMenu && (
                <div className="absolute right-0 top-full mt-1 shadow-lg z-50 border border-gray-200">
                  <button
                    className="flex items-center space-x-1 px-2 py-1 hover:bg-gray-50 w-full text-left transition-colors text-sm"
                    onClick={() => setShowUpdatePost(true)}
                  >
                    <FiEdit2 size={14} />
                    <span>Edit</span>
                  </button>
                  <button
                    className="flex items-center space-x-1 px-2 py-1 hover:bg-red-50 text-red-600 w-full text-left transition-colors text-sm"
                    onClick={handleDelete}
                  >
                    <FiTrash size={14} />
                    <span>Delete</span>
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {imageUrl && (
          <div className="w-full overflow-hidden">
            <img
              src={imageUrl}
              alt="Post"
              className="w-ful aspect-square object-cover"
            />
          </div>
        )}

        <div className="px-4 pb-2 pt-3">
          <p className="text-sm whitespace-pre-wrap break-words">
            {post.content}
          </p>
        </div>

        <div className="px-4 pb-3 pt-2">
          <p className="text-xs text-gray-500">
            {formatDistanceToNow(new Date(post.createdAt), {
              addSuffix: true, //전후 시간표시 사용할꺼냐 말꺼냐 옵션
              locale: ko,
            })}
          </p>
        </div>
      </div>
      {showUpdatePost && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <CreatePost post={post} onClose={() => setShowUpdatePost(false)} />
        </div>
      )}
    </>
  );
};

export default PostCard;
