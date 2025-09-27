import { FiImage, FiX } from "react-icons/fi";
import Avatar from "../common/Avatar";
import { useRef, useState } from "react";
import usePostStore from "../../store/PostStore";
import useAuthStore from "../../store/authStore";
import { redirect } from "react-router-dom";
import axios from "axios";

const CreatePost = ({ post, onClose }) => {
  const { createPost, updatePost, loading, error } = usePostStore();

  const [content, setContent] = useState(post ? post.content : "");
  const { user } = useAuthStore();

  const fileInputRef = useRef(null); //html을 담는 요소

  const [selectedImage, setSelectedImage] = useState(null);
  const [previewImage, setPreviewImage] = useState(null);

  //파일 업로드 로직
  const handleImageSelect = (e) => {
    const file = e.target.files[0]; //파일 정보 출력

    if (!file) return; //파일이 없으면 나감

    if (!file.type.startsWith("image/")) return; //파일 타입이 iamge가 아니면 x

    if (file.size > 5 * 1024 * 1024) return; //파일 크기가 5mb크면 x

    //입력된 파일을 읽는 로직
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement("canvas");
        const size = Math.min(img.width, img.height);
        canvas.width = size;
        canvas.height = size;

        const ctx = canvas.getContext("2d");
        const x = (img.width - size) / 2;
        const y = (img.height - size) / 2;

        ctx.drawImage(img, x, y, size, size, 0, 0, size, size);
        //이미지 파일형태로 만드는 것 / Blob: 2진 파일 => 파일 미리보기 로직
        canvas.toBlob(
          (blob) => {
            setSelectedImage(blob);
            setPreviewImage(canvas.toDataURL("image/jpeg")); //html태그보면 <img src=""/> 이런식으로 URL 을 적어주니까 URL로 받는것
          },
          "image/jpeg",
          0.9 // jpeg 형식
        );
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  };

  //삭제 로직
  const removeImage = () => {
    setSelectedImage(null);
    setPreviewImage(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
      // fileInputRef에 값이 있을수도있을까봐 초기화
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!content.trim()) {
      return;
    }

    if (content.length > 2200) {
      return;
    }

    try {
      let imageUrl = null;

      if (selectedImage) {
        const formData = new FormData();
        formData.append("file", selectedImage, "image.jpg");

        const token = localStorage.getItem("accessToken");

        if (!token || token === "undefined" || token === "null") {
          throw new Error(
            "No valid authenication token found. Please login again."
          );
        }

        const response = await axios.post(
          `${import.meta.env.VITE_API_URL}/api/upload/post`,
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
              Authorization: `Bearer ${token}`,
            },
          }
        );
        imageUrl = response.data.url;
        console.log(imageUrl);
      }

      if (post) {
        await updatePost(post.id, content.trim());
      } else {
        await createPost({
          content: content.trim(),
          imageUrl,
        });
      }

      setContent("");
      onClose();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 max-w-2xl w-full mx-auto">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold">
          {" "}
          {post ? "Update Post" : "Create New Post"}
        </h2>
        <button className="text-gray-500 hover:text-gray-700" onClick={onClose}>
          <FiX size={24} />
        </button>
      </div>

      <form className="space-y-4" onSubmit={handleSubmit}>
        <div className="flex items-start space-x-3">
          <Avatar user={user} size="medium" />
          <div className="flex-1">
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-pink-500"
              placeholder="What's on yout mind?"
              rows="4"
              maxLength={2200}
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
            <div className="text-right text-sm text-gray-500 mt-1">
              {content.length}/2200
            </div>
          </div>
        </div>

        {previewImage && (
          <div className="relative w-1/2 mx-auto">
            <img
              src={previewImage}
              alt="Preview"
              className="w-full rounded-lg object-cover aspect-square"
            />
            <button
              type="button"
              className="absolute top-2 right-2 p-1 bg-black/50 rounded-full text-white hover:bg-black/70"
              onClick={removeImage}
            >
              {" "}
              <FiX size={20} />
            </button>
          </div>
        )}

        {!post && (
          <div>
            {/* 파일형식이 들어오도록 html딴에서 제한하는 설정 */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleImageSelect}
            />
            <button
              type="button"
              className="flex items-center space-x-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              onClick={() => fileInputRef.current?.click()}
            >
              <FiImage size={20} />
              <span>Add Photo</span>
            </button>
          </div>
        )}

        {error && <div className="text-red-500 text-sm">{error}</div>}

        <div className="flex justify-end space-x-3">
          <button
            type="button"
            className="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-6 py-2 bg-gradient-to-r from-purple-500 via-pink-500 to-orange-400 text-white rounded-lg hover:opacity-90 transition-opacity disabled:opacity-50"
            disabled={loading || !content.trim() || content === post?.content} //content === post?.content 있는경우에만 업데이트시 기존내용이 같으면 update불가
          >
            {loading ? "Posting ..." : "Post"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreatePost;
