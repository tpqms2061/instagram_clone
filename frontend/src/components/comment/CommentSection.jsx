import { useState } from "react";
import CommentForm from "./CommentForm";
import CommentItem from "./CommentItem";
import CommentList from "./CommentList";

const CommentSection = ({ post, commentCount, setCommentCount }) => {
  const [comments, setComments] = useState([]);
  return (
    <div className="mt-4">
      <CommentForm
        postId={post.id}
        commentCount={commentCount}
        setCommentCount={setCommentCount}
      />
      {/* post.id만 필요해서 props 로 내림 */}
      <div>
        <CommentList
          postId={post.id}
          comments={comments}
          setComments={setComments}
        />
      </div>
    </div>
  );
};

export default CommentSection;
