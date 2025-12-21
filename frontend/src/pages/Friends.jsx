import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users } from 'lucide-react';
import { getCurrentUserInfo } from '../api/authStorage';
import {
    addFriend,
    getFriendList,
    getPendingFriendRequests,
    getSentFriendRequests,
    handleFriendApplication,
    removeFriend,
} from '../api/friendApi';

const Friends = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [friends, setFriends] = useState([]);
    const [total, setTotal] = useState(0);

    const [pendingLoading, setPendingLoading] = useState(true);
    const [pendingError, setPendingError] = useState('');
    const [pendingList, setPendingList] = useState([]);
    const [pendingTotal, setPendingTotal] = useState(0);
    const [pendingActionKey, setPendingActionKey] = useState('');

    const [sentLoading, setSentLoading] = useState(true);
    const [sentError, setSentError] = useState('');
    const [sentList, setSentList] = useState([]);
    const [sentTotal, setSentTotal] = useState(0);

    const [deletingKey, setDeletingKey] = useState('');

    const [friendUsername, setFriendUsername] = useState('');
    const [adding, setAdding] = useState(false);
    const [actionMsg, setActionMsg] = useState('');
    const [actionErr, setActionErr] = useState('');

    let content;
    if (loading) content = <div className="text-gray-500">加载中...</div>;
    else if (error) content = <div className="text-red-400">{error}</div>;
    else if (friends.length === 0) content = <div className="text-gray-500">暂无好友</div>;
    else {
        content = (
            <div className="space-y-3">
                {friends.map((f) => (
                    <div
                        key={f.friendId}
                        className="flex justify-between items-center py-3 px-4 bg-white/5 rounded-xl"
                    >
                        <div className="flex flex-col">
                            <span className="text-white font-medium">{f.friendUsername}</span>
                            <span className="text-gray-500 text-xs">积分 {f.friendPoints ?? 0}</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <span className="text-gray-400 text-sm">{f.friendshipStatus}</span>
                            <button
                                type="button"
                                onClick={() => handleDeleteFriend(f.friendUsername)}
                                disabled={deletingKey !== ''}
                                className="px-4 py-2 bg-white/10 hover:bg-white/20 rounded-xl transition-all text-white disabled:opacity-50"
                            >
                                {deletingKey === f.friendUsername ? '删除中...' : '删除'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    let pendingContent;
    if (pendingLoading) pendingContent = <div className="text-gray-500">加载中...</div>;
    else if (pendingError) pendingContent = <div className="text-red-400">{pendingError}</div>;
    else if (pendingList.length === 0) pendingContent = <div className="text-gray-500">暂无待处理申请</div>;
    else {
        pendingContent = (
            <div className="space-y-3">
                {pendingList.map((p) => (
                    <div
                        key={p.friendId}
                        className="flex justify-between items-center py-3 px-4 bg-white/5 rounded-xl"
                    >
                        <div className="flex flex-col">
                            <span className="text-white font-medium">{p.friendUsername}</span>
                            <span className="text-gray-500 text-xs">状态 {p.friendshipStatus}</span>
                        </div>

                        <div className="flex gap-2">
                            <button
                                type="button"
                                onClick={() => handlePending(p.friendUsername, 'accept')}
                                disabled={pendingActionKey !== ''}
                                className="px-4 py-2 bg-white/10 hover:bg-white/20 rounded-xl transition-all text-white disabled:opacity-50"
                            >
                                {pendingActionKey === `${p.friendUsername}:accept` ? '处理中...' : '接受'}
                            </button>
                            <button
                                type="button"
                                onClick={() => handlePending(p.friendUsername, 'reject')}
                                disabled={pendingActionKey !== ''}
                                className="px-4 py-2 bg-white/10 hover:bg-white/20 rounded-xl transition-all text-white disabled:opacity-50"
                            >
                                {pendingActionKey === `${p.friendUsername}:reject` ? '处理中...' : '拒绝'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    let sentContent;
    if (sentLoading) sentContent = <div className="text-gray-500">加载中...</div>;
    else if (sentError) sentContent = <div className="text-red-400">{sentError}</div>;
    else if (sentList.length === 0) sentContent = <div className="text-gray-500">暂无已发送申请</div>;
    else {
        sentContent = (
            <div className="space-y-3">
                {sentList.map((s) => (
                    <div
                        key={s.friendId}
                        className="flex justify-between items-center py-3 px-4 bg-white/5 rounded-xl"
                    >
                        <div className="flex flex-col">
                            <span className="text-white font-medium">{s.friendUsername}</span>
                            <span className="text-gray-500 text-xs">状态 {s.friendshipStatus}</span>
                        </div>
                        <span className="text-gray-400 text-sm">等待处理</span>
                    </div>
                ))}
            </div>
        );
    }

    useEffect(() => {
        const userInfo = getCurrentUserInfo();
        if (!userInfo?.username) {
            navigate('/login');
            return;
        }

        let cancelled = false;

        (async () => {
            try {
                setLoading(true);
                setError('');
                const resp = await getFriendList(userInfo.username, { page: 1, size: 50 });
                if (cancelled) return;
                setFriends(resp?.friendList || []);
                setTotal(resp?.total || 0);
            } catch (e) {
                if (cancelled) return;
                setError(e?.message || '好友列表加载失败');
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();

        (async () => {
            try {
                setPendingLoading(true);
                setPendingError('');
                const resp = await getPendingFriendRequests(userInfo.username, { page: 1, size: 50 });
                if (cancelled) return;
                setPendingList(resp?.friendList || []);
                setPendingTotal(resp?.total || 0);
            } catch (e) {
                if (cancelled) return;
                setPendingError(e?.message || '待处理申请加载失败');
            } finally {
                if (!cancelled) setPendingLoading(false);
            }
        })();

        (async () => {
            try {
                setSentLoading(true);
                setSentError('');
                const resp = await getSentFriendRequests(userInfo.username, { page: 1, size: 50 });
                if (cancelled) return;
                setSentList(resp?.friendList || []);
                setSentTotal(resp?.total || 0);
            } catch (e) {
                if (cancelled) return;
                setSentError(e?.message || '已发送申请加载失败');
            } finally {
                if (!cancelled) setSentLoading(false);
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [navigate]);

    const refresh = async () => {
        const userInfo = getCurrentUserInfo();
        if (!userInfo?.username) return;
        const [friendsResp, pendingResp, sentResp] = await Promise.all([
            getFriendList(userInfo.username, { page: 1, size: 50 }),
            getPendingFriendRequests(userInfo.username, { page: 1, size: 50 }),
            getSentFriendRequests(userInfo.username, { page: 1, size: 50 }),
        ]);
        setFriends(friendsResp?.friendList || []);
        setTotal(friendsResp?.total || 0);
        setPendingList(pendingResp?.friendList || []);
        setPendingTotal(pendingResp?.total || 0);
        setSentList(sentResp?.friendList || []);
        setSentTotal(sentResp?.total || 0);
    };

    const handleAddFriend = async () => {
        const userInfo = getCurrentUserInfo();
        if (!userInfo?.username) {
            navigate('/login');
            return;
        }
        const target = friendUsername.trim();
        if (!target) return;

        try {
            setAdding(true);
            setActionErr('');
            setActionMsg('');
            await addFriend(userInfo.username, { friendUsername: target });
            setActionMsg('已发送好友申请，等待对方确认');
            setFriendUsername('');
            await refresh();
        } catch (e) {
            setActionErr(e?.message || '添加好友失败');
        } finally {
            setAdding(false);
        }
    };

    const handlePending = async (friendUsernameToHandle, handleType) => {
        const userInfo = getCurrentUserInfo();
        if (!userInfo?.username) {
            navigate('/login');
            return;
        }

        try {
            setPendingActionKey(`${friendUsernameToHandle}:${handleType}`);
            setActionErr('');
            setActionMsg('');
            await handleFriendApplication(userInfo.username, {
                friendUsername: friendUsernameToHandle,
                handleType,
            });
            setActionMsg(handleType === 'accept' ? '已接受好友申请' : '已拒绝好友申请');
            await refresh();
        } catch (e) {
            setActionErr(e?.message || '操作失败');
        } finally {
            setPendingActionKey('');
        }
    };

    const handleDeleteFriend = async (friendUsernameToDelete) => {
        const userInfo = getCurrentUserInfo();
        if (!userInfo?.username) {
            navigate('/login');
            return;
        }

        try {
            setDeletingKey(friendUsernameToDelete);
            setActionErr('');
            setActionMsg('');
            await removeFriend(userInfo.username, { friendUsername: friendUsernameToDelete });
            setActionMsg('好友删除成功');
            await refresh();
        } catch (e) {
            setActionErr(e?.message || '删除好友失败');
        } finally {
            setDeletingKey('');
        }
    };

    return (
        <div className="min-h-screen pt-24 pb-12 px-6">
            <div className="max-w-4xl mx-auto">
                <div className="glass-dark rounded-3xl p-8 mb-8">
                    <div className="flex items-center gap-3 mb-2">
                        <Users className="w-5 h-5 text-gray-400" />
                        <h2 className="text-2xl font-bold">好友</h2>
                    </div>
                    <p className="text-gray-500 text-sm">共 {total} 位好友</p>

                    <div className="mt-6 flex flex-col gap-3">
                        <div className="flex gap-3">
                            <input
                                type="text"
                                value={friendUsername}
                                onChange={(e) => setFriendUsername(e.target.value)}
                                placeholder="输入对方用户名"
                                className="flex-1 bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white placeholder-gray-600 focus:outline-none focus:border-apple-orange transition-all"
                            />
                            <button
                                type="button"
                                onClick={handleAddFriend}
                                disabled={adding}
                                className="px-5 py-3 bg-white/10 hover:bg-white/20 rounded-xl transition-all text-white disabled:opacity-50"
                            >
                                {adding ? '添加中...' : '添加好友'}
                            </button>
                        </div>
                        {actionMsg ? <div className="text-green-400 text-sm">{actionMsg}</div> : null}
                        {actionErr ? <div className="text-red-400 text-sm">{actionErr}</div> : null}
                    </div>
                </div>

                <div className="glass-dark rounded-3xl p-8">
                    <div className="mb-6">
                        <div className="flex items-center justify-between mb-2">
                            <h3 className="text-white font-semibold">待处理申请</h3>
                            <span className="text-gray-500 text-sm">{pendingTotal} 条</span>
                        </div>

                        {pendingContent}
                    </div>

                    <div className="mb-6">
                        <div className="flex items-center justify-between mb-2">
                            <h3 className="text-white font-semibold">已发送申请</h3>
                            <span className="text-gray-500 text-sm">{sentTotal} 条</span>
                        </div>

                        {sentContent}
                    </div>

                    {content}
                </div>
            </div>
        </div>
    );
};

export default Friends;
