import { de } from "date-fns/locale";
import { FiArrowLeft, FiBookmark, FiGrid, FiLock } from "react-icons/fi";
import Avatar from "../components/common/Avatar";
import { Link, useParams } from "react-router-dom";
import useFollowStore from "../store/followStore";
import { useEffect, useState } from "react";
import useUserStore from "../store/userStore";
import useAuthStore from "../store/authStore";

const Profile = () => {
  const { username } = useParams();

  const { followStatus, getFollowStatus } = useFollowStore();
  const { userProfile, getUserProfile } = useUserStore(); //접속한 상태의 프로필
  const { user: currentUser } = useAuthStore(); //내프로필

  const [currentFollwStatus, setCurrentFollowStatus] = useState();

  const isOwnProfile = currentUser?.username === userProfile?.username;
  console.log("isOwnProfile", isOwnProfile);

  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const { userProfile } = await getUserProfile(username);
        if (userProfile) {
          console.log(userProfile);
        }
      } catch (err) {
        console.log(err);
      }
    };
    loadUserProfile();
  }, [getUserProfile]);

  useEffect(() => {
    const loadFollowStatus = async () => {
      if (userProfile) {
        try {
          const result = await getFollowStatus(userProfile.id);
          setCurrentFollowStatus(result[userProfile.id]);
        } catch (err) {
          console.log(err);
        }
      }
    };
    loadFollowStatus();
  }, [userProfile, getFollowStatus]);

  return (
    <div className=" bg-gray-50">
      <div className="bg-white max-w-2xl mx-auto">
        <header className="bg-white border-b border-gray-50-300 sticky top-0 z-40">
          <div className="flex items-center justify-between px-4 py-4 ">
            <Link className="text-gray-700 hover:text-black" to="/">
              <FiArrowLeft size={24} />
            </Link>
            <h1 className="font-semibold text-lg">{userProfile?.username}</h1>
            <div className="w-6"></div>
          </div>
        </header>

        <div className="bg-white p-4 border-b border-gray-300">
          <div className="flex items-start space-x-4">
            <Avatar user={userProfile} size="large" />

            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-xl font-semibold">
                  {userProfile?.username}
                </h2>
                {isOwnProfile ? (
                  <button className="px-4 py-1 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-500">
                    Edit Profile
                  </button>
                ) : (
                  <button className="px-4 py-1 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-500">
                    Follow
                  </button>
                )}
              </div>

              <p className="font-semibold text-sm">{userProfile?.fullName}</p>
              <p className="text-sm mt-1"> {userProfile?.bio} </p>
            </div>
          </div>
        </div>
        <div className="flex justify-around mt-6 pt-4 ">
          <div className="text-center">
            <p className="font-semibold">
              {currentFollwStatus?.followersCount || 0}
            </p>
            <p className="text-gray-500 text-sm">posts</p>
          </div>
          <button className="text-center hover:opacity-70 transition-opacity cursor-pointer">
            <p className="font-semibold">
              {currentFollwStatus?.followingCount || 0}
            </p>
            <p className="text-gray-500 text-sm">followers</p>
          </button>
          <button className="text-center hover:opacity-70 transition-opacity cursor-pointer">
            <p className="font-semibold">000</p>
            <p className="text-gray-500 text-sm">following</p>
          </button>
        </div>

        <div className="border-b border-gray-300">
          <div className="flex justify-center">
            <button className="flex-1 py-3 flex items-center justify-center border-b-2 border-black text-black">
              <FiGrid size={20} />
            </button>
            <button className="flex-1 py-3 flex items-center justify-center text-gray-500">
              <FiBookmark size={20} />
            </button>
          </div>
        </div>
        <div className="p-4">
          <div className="text-center py-12">
            <FiLock size={40} className="mx-auto mb-2 text-gray-400" />
            <p className="text-gray-500"> This is private</p>
            <p className="text-sm text-gray-400 mt-1">
              Only the owner can see saved posts
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
