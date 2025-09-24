import { Link } from "react-router-dom";
import Avatar from "../common/Avatar";
import { ko } from "date-fns/locale";
import { formatDistance, formatDistanceToNow } from "date-fns";

const PostCard = ({ post }) => {
  return (
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
      </div>

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
  );
};

export default PostCard;
