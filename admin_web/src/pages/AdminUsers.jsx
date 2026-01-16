import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue, update, set, remove } from "firebase/database";

function safe(v, fb = "") {
  return v == null ? fb : v;
}

export default function AdminUsers() {
  // ======================
  // USERS realtime
  // ======================
  const [usersMap, setUsersMap] = useState({});
  const [selectedUid, setSelectedUid] = useState(null);
  const [q, setQ] = useState("");

  useEffect(() => {
    const r = ref(db, "users");
    const unsub = onValue(r, (snap) => setUsersMap(snap.val() || {}));
    return () => unsub();
  }, []);

  const usersArr = useMemo(() => {
    const arr = Object.entries(usersMap).map(([uid, u]) => ({ uid, ...u }));
    const keyword = q.trim().toLowerCase();
    if (!keyword) return arr;

    return arr.filter((u) => {
      const name = String(u.name || "").toLowerCase();
      const email = String(u.email || "").toLowerCase();
      const role = String(u.role || "").toLowerCase();
      const uid2 = String(u.uid || u.userId || "").toLowerCase();
      return (
        name.includes(keyword) ||
        email.includes(keyword) ||
        role.includes(keyword) ||
        uid2.includes(keyword) ||
        String(u.uid || "").toLowerCase().includes(keyword) ||
        String(u.userId || "").toLowerCase().includes(keyword)
      );
    });
  }, [usersMap, q]);

  const selectedUser = selectedUid ? usersMap[selectedUid] : null;

  // ======================
  // BASIC INFO edit
  // ======================
  const [editName, setEditName] = useState("");
  useEffect(() => {
    setEditName(selectedUser?.name || "");
    setSelectedAlbumId(null);
  }, [selectedUid]); // eslint-disable-line

  const saveUserName = async () => {
    if (!selectedUid) return;
    await update(ref(db, `users/${selectedUid}`), { name: editName });
    alert("✅ Đã cập nhật tên user.");
  };

  const setRoleUser = async (role) => {
    if (!selectedUid) return;
    await update(ref(db, `users/${selectedUid}`), { role });
    alert("✅ Đã cập nhật role = " + role);
  };

  // ======================
  // SONGS realtime (pick đúng bài - không lệch id)
  // ======================
  const [songsMap, setSongsMap] = useState({});
  useEffect(() => {
    const r = ref(db, "songs");
    const unsub = onValue(r, (snap) => setSongsMap(snap.val() || {}));
    return () => unsub();
  }, []);

  const songsPickArr = useMemo(() => {
    const arr = Object.entries(songsMap).map(([firebaseKey, s]) => ({
      firebaseKey,
      songId: String(s?.id ?? firebaseKey),
      ...s,
    }));
    arr.sort((a, b) => String(b.songId).localeCompare(String(a.songId)));
    return arr;
  }, [songsMap]);

  // ======================
  // ALBUMS
  // ======================
  const [selectedAlbumId, setSelectedAlbumId] = useState(null);

  const albumsArr = useMemo(() => {
    if (!selectedUser?.albums) return [];
    return Object.entries(selectedUser.albums).map(([albumId, a]) => ({
      albumId,
      ...a,
    }));
  }, [selectedUser]);

  const selectedAlbum = useMemo(() => {
    if (!selectedUser?.albums || !selectedAlbumId) return null;
    return selectedUser.albums[selectedAlbumId] || null;
  }, [selectedUser, selectedAlbumId]);

  const albumSongsArr = useMemo(() => {
    if (!selectedAlbum?.songs) return [];
    return Object.entries(selectedAlbum.songs).map(([songKey, s]) => ({
      songKey,
      ...s,
    }));
  }, [selectedAlbum]);

  // ======================
  // CREATE / RENAME / DELETE ALBUM
  // ======================
  const [openAddAlbum, setOpenAddAlbum] = useState(false);
  const [newAlbumName, setNewAlbumName] = useState("");

  const createAlbum = async () => {
    if (!selectedUid) return;
    const name = newAlbumName.trim();
    if (!name) return alert("Nhập tên album");

    const albumId = `alb_${Date.now()}`;

    await set(ref(db, `users/${selectedUid}/albums/${albumId}`), {
      id: albumId,
      name,
      imageUrl: "",
      songs: {},
    });

    setNewAlbumName("");
    setOpenAddAlbum(false);
    setSelectedAlbumId(albumId);
    alert("✅ Đã tạo album.");
  };

  const renameAlbum = async (albumId, name) => {
    if (!selectedUid) return;
    const n = String(name || "").trim();
    if (!n) return alert("Tên album không được rỗng");
    await update(ref(db, `users/${selectedUid}/albums/${albumId}`), { name: n });
    alert("✅ Đã đổi tên album.");
  };

  const deleteAlbum = async (albumId) => {
    if (!selectedUid) return;
    const ok = window.confirm("Xóa album này? (mất luôn songs trong album)");
    if (!ok) return;

    await remove(ref(db, `users/${selectedUid}/albums/${albumId}`));
    if (selectedAlbumId === albumId) setSelectedAlbumId(null);
    alert("✅ Đã xóa album.");
  };

  // ======================
  // PICK SONG MODAL
  // chọn theo firebaseKey => không bị id +1
  // ======================
  const [openPickSong, setOpenPickSong] = useState(false);
  const [songPickQ, setSongPickQ] = useState("");
  const [pickedFirebaseKey, setPickedFirebaseKey] = useState("");

  const songsPickFiltered = useMemo(() => {
    const k = songPickQ.trim().toLowerCase();
    if (!k) return songsPickArr;
    return songsPickArr.filter((s) => {
      const t = `${s.songId} ${s.title} ${s.artist} ${s.category}`.toLowerCase();
      return t.includes(k);
    });
  }, [songsPickArr, songPickQ]);

  const addSongToAlbum = async () => {
    if (!selectedUid) return alert("Chọn user trước");
    if (!selectedAlbumId) return alert("Chọn album trước");
    if (!pickedFirebaseKey) return alert("Chọn 1 bài từ /songs trước");

    const pickedSong = songsMap[pickedFirebaseKey];
    if (!pickedSong) return alert("Không tìm thấy bài trong /songs");

    const songKey = String(pickedSong.id ?? pickedFirebaseKey);

    await set(ref(db, `users/${selectedUid}/albums/${selectedAlbumId}/songs/${songKey}`), {
      id: songKey,
      title: safe(pickedSong.title, ""),
      artist: safe(pickedSong.artist, ""),
      category: safe(pickedSong.category, ""),
      coverUrl: safe(pickedSong.coverUrl, ""),
      mp3Url: safe(pickedSong.mp3Url, ""),
    });

    setPickedFirebaseKey("");
    setOpenPickSong(false);
    alert("✅ Đã thêm bài vào album.");
  };

  const removeSongFromAlbum = async (songKey) => {
    if (!selectedUid || !selectedAlbumId) return;
    const ok = window.confirm("Xóa bài này khỏi album?");
    if (!ok) return;

    await remove(ref(db, `users/${selectedUid}/albums/${selectedAlbumId}/songs/${songKey}`));
    alert("✅ Đã xóa bài khỏi album.");
  };

  return (
    <div>
      {/* TITLE */}
      <div className="page-title">
        <div>
          <h1>Users</h1>
          
        </div>
      </div>

      {/* 2 CỘT - GIỮ Y HÌNH */}
      <div className="users-shell">
        {/* LEFT */}
        <div className="card users-left">
          <input
            className="input"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Tìm user: name/email/uid/role..."
          />

          <div className="list">
            {usersArr.map((u) => {
              const active = selectedUid === u.uid;
              return (
                <button
                  key={u.uid}
                  className={`row user-item ${active ? "active" : ""}`}
                  onClick={() => setSelectedUid(u.uid)}
                  style={{ border: "1px solid var(--border)" }}
                >
                  <div className="row-title">
                    {u.name || "(no name)"} <span style={{ color: "var(--muted)", fontWeight: 800 }}>• {u.role || "user"}</span>
                  </div>
                  <div className="row-sub">{u.email || "(no email)"}</div>
                  <div className="row-sub">UID: {u.uid || u.userId || "(missing)"}</div>
                </button>
              );
            })}

            {usersArr.length === 0 && <div className="note">Không có users.</div>}
          </div>
        </div>

        {/* RIGHT */}
        <div className="card users-right">
          {!selectedUid ? (
            <div className="note">Chọn 1 user bên trái để xem chi tiết.</div>
          ) : (
            <>
              <h2 style={{ marginTop: 0 }}>User Detail: {selectedUid}</h2>

              {/* BASIC + AVATARPATH */}
              <div className="grid2" style={{ marginTop: 10 }}>
                <div className="card users-subcard">
                  <div className="users-subtitle">Thông tin cơ bản</div>

                  <div className="users-line">
                    Email: <b>{safe(selectedUser?.email, "(none)")}</b>
                  </div>

                  <div className="users-line">
                    Role: <b>{safe(selectedUser?.role, "user")}</b>
                  </div>

                  <div className="label">Sửa tên:</div>
                  <input className="input" value={editName} onChange={(e) => setEditName(e.target.value)} />

                  <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
                    <button className="btn" onClick={saveUserName}>
                      Lưu tên
                    </button>
                  </div>

                  <div className="note" style={{ marginTop: 12 }}>
                    (Tuỳ chọn) đổi role:
                  </div>
                  <div style={{ marginTop: 10, display: "flex", gap: 10, flexWrap: "wrap" }}>
                  <button
                      className={`btn ${selectedUser?.role === "user" ? "btn-primary" : ""}`}
                      onClick={() => setRoleUser("user")}
                    >
                      Set user
                    </button>

                    <button
                      className={`btn ${selectedUser?.role === "admin" ? "btn-primary" : ""}`}
                      onClick={() => setRoleUser("admin")}
                    >
                      Set admin
                    </button>

                  </div>
                </div>

                <div className="card users-subcard">
                  <div className="users-subtitle">AvatarPath (text)</div>
                  <div className="row-sub" style={{ wordBreak: "break-word" }}>
                    {safe(selectedUser?.avatarPath, "(none)")}
                  </div>
                  <div className="note">* Bạn bảo không cần ảnh → chỉ hiển thị đường dẫn cũ.</div>
                </div>
              </div>

              {/* ALBUMS */}
              <div className="users-albums">
                <div className="users-albums-head">
                  <h2 style={{ margin: 0 }}>Albums của user</h2>
                  <button className="btn btn-primary" onClick={() => { setNewAlbumName(""); setOpenAddAlbum(true); }}>
                    + Tạo album
                  </button>
                </div>

                <div className="users-albums-grid">
                  {/* album list */}
                  <div className="card users-subcard">
                    {albumsArr.length === 0 ? (
                      <div className="note">User chưa có album.</div>
                    ) : (
                      <div className="list" style={{ maxHeight: "48vh" }}>
                        {albumsArr.map((a) => {
                          const active = selectedAlbumId === a.albumId;
                          return (
                            <div key={a.albumId} className={`row album-item ${active ? "active" : ""}`}>
                              <button className="album-btn" onClick={() => setSelectedAlbumId(a.albumId)}>
                                <div className="row-title">{a.name || "(no name)"}</div>
                                <div className="row-sub">albumId: {a.albumId}</div>
                              </button>

                              <div className="row-actions" style={{ marginTop: 10 }}>
                                <button
                                  className="btn"
                                  onClick={() => {
                                    const newName = prompt("Nhập tên album mới:", a.name || "");
                                    if (newName != null) renameAlbum(a.albumId, newName);
                                  }}
                                >
                                  Rename
                                </button>
                                <button className="btn" style={{ borderColor: "#fecaca", color: "#b91c1c" }} onClick={() => deleteAlbum(a.albumId)}>
                                  Delete
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>

                  {/* album songs */}
                  <div className="card users-subcard">
                    {!selectedAlbumId ? (
                      <div className="note">Chọn 1 album để xem / chỉnh sửa nhạc.</div>
                    ) : (
                      <>
                        <div className="row-title">Album: {safe(selectedAlbum?.name, selectedAlbumId)}</div>

                        <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
                          <button className="btn" onClick={() => setOpenPickSong(true)}>
                            Chọn bài từ /songs
                          </button>
                          <button className="btn btn-primary" onClick={addSongToAlbum}>
                            + Add vào album
                          </button>
                        </div>

                        <div className="note">
                          Đã chọn: <b>{pickedFirebaseKey ? pickedFirebaseKey : "(chưa chọn)"}</b>
                          <div>* Fix lệch bài: chọn theo firebaseKey nên không bị “id +1”.</div>
                        </div>

                        <div className="list" style={{ maxHeight: "48vh" }}>
                          {albumSongsArr.length === 0 ? (
                            <div className="note">Album chưa có bài.</div>
                          ) : (
                            albumSongsArr.map((s) => (
                              <div key={s.songKey} className="row">
                                <div className="row-head">
                                  <div>
                                    <div className="row-title">
                                      {s.title || "(no title)"}{" "}
                                      <span style={{ color: "var(--muted)", fontWeight: 800 }}>• {s.artist || "(no artist)"}</span>
                                    </div>
                                  </div>
                                  <div className="row-actions">
                                    <button
                                      className="btn"
                                      style={{ borderColor: "#fecaca", color: "#b91c1c" }}
                                      onClick={() => removeSongFromAlbum(s.songKey)}
                                    >
                                      Remove
                                    </button>
                                  </div>
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* PICK SONG MODAL */}
      {openPickSong && (
        <Modal title="🎵 Chọn bài từ /songs" onClose={() => setOpenPickSong(false)}>
          <input
            className="input"
            value={songPickQ}
            onChange={(e) => setSongPickQ(e.target.value)}
            placeholder="Search title/artist/category/id..."
          />

          <div className="list" style={{ maxHeight: "60vh" }}>
            {songsPickFiltered.map((s) => {
              const active = pickedFirebaseKey === s.firebaseKey;
              return (
                <button
                  key={s.firebaseKey}
                  className={`row pick-item ${active ? "active" : ""}`}
                  onClick={() => {
                    setPickedFirebaseKey(s.firebaseKey);
                    setOpenPickSong(false);
                  }}
                  style={{ textAlign: "left", width: "100%", cursor: "pointer" }}
                >
                  <div className="row-title">
                    {s.title || "(no title)"}{" "}
                    <span style={{ color: "var(--muted)", fontWeight: 800 }}>• {s.artist || "(no artist)"}</span>
                  </div>
                  <div className="row-sub">
                    {s.category ? `${s.category} • ` : ""}
                    songId: {s.songId} • key: {s.firebaseKey}
                  </div>
                </button>
              );
            })}

            {songsPickFiltered.length === 0 && <div className="note">Không tìm thấy bài.</div>}
          </div>
        </Modal>
      )}

      {/* CREATE ALBUM MODAL */}
      {openAddAlbum && (
        <Modal title="➕ Tạo album mới" onClose={() => setOpenAddAlbum(false)}>
          <div className="label">Tên album</div>
          <input
            className="input"
            value={newAlbumName}
            onChange={(e) => setNewAlbumName(e.target.value)}
            placeholder="Ví dụ: My Favorite"
          />

          <div style={{ display: "flex", gap: 10, marginTop: 14, flexWrap: "wrap" }}>
            <button className="btn btn-primary" onClick={createAlbum}>
              Create
            </button>
            <button className="btn" onClick={() => setOpenAddAlbum(false)}>
              Cancel
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}

/* ====================== MODAL ====================== */
function Modal({ title, onClose, children }) {
  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="t">{title}</div>
          <button className="btn" onClick={onClose}>
            ✕
          </button>
        </div>
        <div style={{ marginTop: 12 }}>{children}</div>
      </div>
    </div>
  );
}
