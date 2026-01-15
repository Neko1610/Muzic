import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue, set, update, remove } from "firebase/database";

function safe(v, fb = "") {
  return v == null ? fb : v;
}

export default function AdminPlaylists() {
  // ======================
  // PLAYLISTS realtime
  // ======================
  const [plMap, setPlMap] = useState({});
  const [selectedPlId, setSelectedPlId] = useState(null);

  useEffect(() => {
    const r = ref(db, "playlists");
    const unsub = onValue(r, (snap) => setPlMap(snap.val() || {}));
    return () => unsub();
  }, []);

  const playlists = useMemo(() => {
    const arr = Object.entries(plMap).map(([playlistId, p]) => ({
      playlistId,
      ...p,
    }));
    arr.sort((a, b) => String(b.playlistId).localeCompare(String(a.playlistId)));
    return arr;
  }, [plMap]);

  const selected = selectedPlId ? plMap[selectedPlId] : null;

  const playlistSongs = useMemo(() => {
    if (!selected?.songs) return [];
    return Object.entries(selected.songs).map(([songKey, s]) => ({
      songKey,
      ...s,
    }));
  }, [selected]);

  // ======================
  // SONGS realtime (picker)
  // ======================
  const [songsMap, setSongsMap] = useState({});
  useEffect(() => {
    const r = ref(db, "songs");
    const unsub = onValue(r, (snap) => setSongsMap(snap.val() || {}));
    return () => unsub();
  }, []);

  const songsArr = useMemo(() => {
    const arr = Object.entries(songsMap).map(([firebaseKey, s]) => ({
      firebaseKey,
      songId: String(s?.id ?? firebaseKey),
      ...s,
    }));
    arr.sort((a, b) => String(b.songId).localeCompare(String(a.songId)));
    return arr;
  }, [songsMap]);

  // ======================
  // CREATE / DELETE
  // ======================
  const [openCreate, setOpenCreate] = useState(false);
  const [newName, setNewName] = useState("");

  const createPlaylist = async () => {
    const name = newName.trim();
    if (!name) return alert("Nhập tên playlist");

    const playlistId = `pl_${Date.now()}`;
    await set(ref(db, `playlists/${playlistId}`), {
      id: playlistId,
      name,
      imageUrl: "",
      songs: {},
    });

    setNewName("");
    setOpenCreate(false);
    setSelectedPlId(playlistId);
  };

  const deletePlaylist = async (playlistId) => {
    const ok = window.confirm("Xóa playlist này? (mất luôn songs trong playlist)");
    if (!ok) return;

    await remove(ref(db, `playlists/${playlistId}`));
    if (selectedPlId === playlistId) setSelectedPlId(null);
  };

  // ======================
  // RENAME (MODAL)
  // ======================
  const [openRename, setOpenRename] = useState(false);
  const [renameId, setRenameId] = useState("");
  const [renameValue, setRenameValue] = useState("");

  const openRenameModal = (playlistId) => {
    const cur = plMap[playlistId];
    setRenameId(playlistId);
    setRenameValue(cur?.name || "");
    setOpenRename(true);
  };

  const submitRename = async () => {
    if (!renameId) return;

    const n = renameValue.trim();
    if (!n) return alert("Tên không được rỗng");

    await update(ref(db, `playlists/${renameId}`), { name: n });

    setOpenRename(false);
    setRenameId("");
    setRenameValue("");
  };

  // ======================
  // PICK SONG (7 items / “stream”)
  // ======================
  const [openPick, setOpenPick] = useState(false);
  const [pickQ, setPickQ] = useState("");
  const [pickedKey, setPickedKey] = useState("");
  const [limit, setLimit] = useState(7);

  const songsFiltered = useMemo(() => {
    const k = pickQ.trim().toLowerCase();
    const base = !k
      ? songsArr
      : songsArr.filter((s) => {
          const t = `${s.songId} ${s.title} ${s.artist} ${s.category}`.toLowerCase();
          return t.includes(k);
        });
    return base.slice(0, limit);
  }, [songsArr, pickQ, limit]);

  const openPicker = () => {
    if (!selectedPlId) return alert("Chọn playlist trước");
    setPickQ("");
    setPickedKey("");
    setLimit(7);
    setOpenPick(true);
  };

  const loadMore = () => setLimit((p) => p + 7);

  const addPickedSong = async () => {
    if (!selectedPlId) return alert("Chọn playlist trước");
    if (!pickedKey) return alert("Chọn 1 bài trước");

    const s = songsMap[pickedKey];
    if (!s) return alert("Không tìm thấy bài trong /songs");

    const songKey = String(s.id ?? pickedKey);

    await set(ref(db, `playlists/${selectedPlId}/songs/${songKey}`), {
      id: songKey,
      title: safe(s.title, ""),
      artist: safe(s.artist, ""),
      category: safe(s.category, ""),
      coverUrl: safe(s.coverUrl, ""),
      mp3Url: safe(s.mp3Url, ""),
    });

    // giữ selected key để thấy “đã chọn”
    setOpenPick(false);
  };

  const removeSongFromPlaylist = async (songKey) => {
    if (!selectedPlId) return;
    const ok = window.confirm("Xóa bài này khỏi playlist?");
    if (!ok) return;

    await remove(ref(db, `playlists/${selectedPlId}/songs/${songKey}`));
  };

  return (
    <div>
      {/* Header dùng style chung */}
      <div className="page-title">
        <div>
          <h1>Playlists</h1>
          <p>
           
          </p>
        </div>

        {/* ✅ FIX: phải có btn + btn-primary để ăn spacing */}
        <button className="btn btn-primary" onClick={() => setOpenCreate(true)}>
          + Create Playlist
        </button>
      </div>

      {/* Layout playlists */}
      <div className="pl-grid">
        {/* LEFT */}
        <div className="card pl-card">
          <div className="pl-left-head">Danh sách playlists ({playlists.length})</div>

          {/* ✅ FIX spacing: dùng list chung */}
          <div className="list">
            {playlists.map((p) => (
              <div
                key={p.playlistId}
                className={`pl-item ${selectedPlId === p.playlistId ? "is-active" : ""}`}
              >
                <button className="pl-item-btn" onClick={() => setSelectedPlId(p.playlistId)}>
                  <div className="pl-item-name">{p.name || "(no name)"}</div>
                  <div className="pl-item-sub">id: {p.playlistId}</div>
                </button>

                <div className="row-actions">
                  <button className="btn" onClick={() => openRenameModal(p.playlistId)}>
                    Rename
                  </button>
                  {/* ✅ FIX: btn + btn-danger */}
                  <button className="btn btn-danger" onClick={() => deletePlaylist(p.playlistId)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}

            {playlists.length === 0 && <div className="note">Chưa có playlist.</div>}
          </div>
        </div>

        {/* RIGHT */}
        <div className="card pl-card">
          {!selectedPlId ? (
            <div className="note">Chọn 1 playlist để xem chi tiết.</div>
          ) : (
            <>
              <div className="pl-title">Playlist: {safe(selected?.name, selectedPlId)}</div>

              <div className="pl-toolbar">
                <button className="btn" onClick={openPicker}>
                  Chọn bài từ /songs
                </button>
                {/* ✅ FIX: btn + btn-primary */}
                <button className="btn btn-primary" onClick={addPickedSong}>
                  + Add vào playlist
                </button>
              </div>

              <div className="pl-picked">
                Đã chọn key: <b>{pickedKey || "(chưa chọn)"}</b>
              </div>

              <div className="pl-section">Songs trong playlist ({playlistSongs.length})</div>

              <div className="pl-songs">
                {playlistSongs.length === 0 ? (
                  <div className="note">Playlist chưa có bài.</div>
                ) : (
                  playlistSongs.map((s) => (
                    <div key={s.songKey} className="pl-song-row">
                      <div className="pl-song-text">
                        {s.title || "(no title)"}{" "}
                        <span className="pl-song-artist">• {s.artist || "(no artist)"}</span>
                      </div>

                      {/* ✅ FIX: btn + btn-danger */}
                      <button
                        className="btn btn-danger"
                        onClick={() => removeSongFromPlaylist(s.songKey)}
                      >
                        Remove
                      </button>
                    </div>
                  ))
                )}
              </div>
            </>
          )}
        </div>
      </div>

      {/* CREATE MODAL */}
      {openCreate && (
        <Modal title="➕ Create Playlist" onClose={() => setOpenCreate(false)}>
          <div className="label">Name</div>
          <input
            className="input"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            placeholder="Nhập tên playlist..."
          />

          <div className="pl-modal-actions">
            <button className="btn btn-primary" onClick={createPlaylist}>
              Create
            </button>
            <button className="btn" onClick={() => setOpenCreate(false)}>
              Cancel
            </button>
          </div>
        </Modal>
      )}

      {/* RENAME MODAL */}
      {openRename && (
        <Modal title="✏️ Rename Playlist" onClose={() => setOpenRename(false)}>
          <div className="label">Tên playlist mới</div>
          <input
            className="input"
            value={renameValue}
            onChange={(e) => setRenameValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") submitRename();
            }}
            placeholder="Nhập tên mới..."
          />

          <div className="pl-modal-actions">
            <button className="btn btn-primary" onClick={submitRename}>
              Save
            </button>
            <button className="btn" onClick={() => setOpenRename(false)}>
              Cancel
            </button>
          </div>
        </Modal>
      )}

      {/* PICK MODAL */}
      {openPick && (
        <Modal title="🎵 Pick song from /songs (7 items)" onClose={() => setOpenPick(false)}>
          <input
            className="input"
            value={pickQ}
            onChange={(e) => {
              setPickQ(e.target.value);
              setLimit(7);
            }}
            placeholder="Search title/artist/category/id..."
          />

          <div className="pl-pick-list">
            {songsFiltered.map((s) => (
              <button
                key={s.firebaseKey}
                className={`pl-pick-row ${pickedKey === s.firebaseKey ? "is-picked" : ""}`}
                onClick={() => {
                  setPickedKey(s.firebaseKey);
                  setOpenPick(false);
                }}
              >
                <div className="pl-pick-title">
                  {s.title || "(no title)"}{" "}
                  <span className="pl-pick-artist">• {s.artist || "(no artist)"}</span>
                </div>
                <div className="pl-pick-sub">
                  {s.category ? `${s.category} • ` : ""}songId: {s.songId} • key: {s.firebaseKey}
                </div>
              </button>
            ))}

            {songsFiltered.length === 0 && <div className="note">Không có bài.</div>}

            <button className="btn" onClick={loadMore}>
              Load 7 more
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}

/* ===== MODAL dùng CSS chung admin.css ===== */
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
