import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue, set, update, remove, get } from "firebase/database";

const emptySong = {
  title: "",
  artist: "",
  category: "",
  coverUrl: "",
  mp3Url: "",
};

export default function SongsPage() {
  // ===== Realtime data =====
  const [songsMap, setSongsMap] = useState({});
  const [loading, setLoading] = useState(true);

  // ===== Search =====
  const [q, setQ] = useState("");

  // ===== Categories =====
  const [categories, setCategories] = useState(["KPop", "VPop", "USUK", "EDM", "Podcast"]);

  // ===== Add modal =====
  const [openAdd, setOpenAdd] = useState(false);
  const [addForm, setAddForm] = useState({ ...emptySong });
  const [adding, setAdding] = useState(false);

  // ===== Edit modal =====
  const [openEdit, setOpenEdit] = useState(false);
  const [editKey, setEditKey] = useState("");
  const [editForm, setEditForm] = useState({ ...emptySong });
  const [editing, setEditing] = useState(false);

  // =========================
  // LOAD SONGS (Realtime)
  // =========================
  useEffect(() => {
    const r = ref(db, "songs");
    const unsub = onValue(
      r,
      (snap) => {
        setSongsMap(snap.val() || {});
        setLoading(false);
      },
      () => setLoading(false)
    );
    return () => unsub();
  }, []);

  // =========================
  // LOAD CATEGORIES (optional)
  // Node: /categories
  // =========================
  useEffect(() => {
    (async () => {
      try {
        const snap = await get(ref(db, "categories"));
        const val = snap.val();
        if (!val) return;

        const arr = [];
        Object.values(val).forEach((c) => {
          if (!c) return;
          if (typeof c === "string") arr.push(c);
          else if (typeof c === "object" && c.name) arr.push(c.name);
        });

        if (arr.length) setCategories(arr);
      } catch (e) {
        // ok
      }
    })();
  }, []);

  // =========================
  // MAP -> ARRAY + SORT + FILTER
  // =========================
  const songsArr = useMemo(() => {
    const arr = Object.entries(songsMap).map(([dbKey, song]) => ({
      dbKey,
      id: song?.id ?? dbKey,
      ...song,
    }));

    arr.sort((a, b) => String(b.id).localeCompare(String(a.id)));

    const keyword = q.trim().toLowerCase();
    if (!keyword) return arr;

    return arr.filter((s) => {
      const text = `${s.id} ${s.title} ${s.artist} ${s.category}`.toLowerCase();
      return text.includes(keyword);
    });
  }, [songsMap, q]);

  // =========================
  // HELPERS
  // =========================
  const generateId = () => `${Date.now()}_${Math.floor(Math.random() * 1000)}`;
  const normalizeUrl = (url) => (url || "").trim();

  // =========================
  // ADD SONG
  // =========================
  const submitAdd = async () => {
    const title = addForm.title.trim();
    const artist = addForm.artist.trim();
    const category = addForm.category.trim();
    const coverUrl = normalizeUrl(addForm.coverUrl);
    const mp3Url = normalizeUrl(addForm.mp3Url);

    if (!title) return alert("Nhập title");
    if (!artist) return alert("Nhập artist");
    if (!category) return alert("Chọn category");
    if (!coverUrl) return alert("Dán coverUrl (link ảnh)");
    if (!mp3Url) return alert("Dán mp3Url (link mp3)");

    setAdding(true);
    try {
      const newKey = generateId();
      await set(ref(db, `songs/${newKey}`), {
        id: newKey,
        title,
        artist,
        category,
        coverUrl,
        mp3Url,
      });

      setAddForm({ ...emptySong });
      setOpenAdd(false);
    } catch (e) {
      alert("Lỗi add song: " + e.message);
    } finally {
      setAdding(false);
    }
  };

  // =========================
  // OPEN EDIT
  // =========================
  const openEditSong = (song) => {
    setEditKey(song.dbKey);
    setEditForm({
      title: song.title || "",
      artist: song.artist || "",
      category: song.category || "",
      coverUrl: song.coverUrl || "",
      mp3Url: song.mp3Url || "",
    });
    setOpenEdit(true);
  };

  // =========================
  // SAVE EDIT
  // =========================
  const submitEdit = async () => {
    if (!editKey) return;

    const title = editForm.title.trim();
    const artist = editForm.artist.trim();
    const category = editForm.category.trim();
    const coverUrl = normalizeUrl(editForm.coverUrl);
    const mp3Url = normalizeUrl(editForm.mp3Url);

    if (!title) return alert("Nhập title");
    if (!artist) return alert("Nhập artist");
    if (!category) return alert("Chọn category");
    if (!coverUrl) return alert("Dán coverUrl");
    if (!mp3Url) return alert("Dán mp3Url");

    setEditing(true);
    try {
      await update(ref(db, `songs/${editKey}`), {
        id: editKey,
        title,
        artist,
        category,
        coverUrl,
        mp3Url,
      });

      setOpenEdit(false);
      setEditKey("");
    } catch (e) {
      alert("Lỗi update: " + e.message);
    } finally {
      setEditing(false);
    }
  };

  // =========================
  // DELETE
  // =========================
  const deleteSong = async (dbKey) => {
    const ok = window.confirm("Xóa bài hát này?");
    if (!ok) return;

    try {
      await remove(ref(db, `songs/${dbKey}`));
    } catch (e) {
      alert("Lỗi delete: " + e.message);
    }
  };

  return (
    <div>
      {/* TITLE */}
      <div className="page-title">
        <div>
          <h1>Songs</h1>
          <p>
            
          </p>
        </div>

        <button className="btn btn-primary" onClick={() => setOpenAdd(true)}>
          ➕ Add Song
        </button>
      </div>

      {/* CARD */}
      <div className="card songs-card" style={{ padding: 16 }}>
        <input
          className="input"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search by id/title/artist/category..."
        />

        <div className="songs-head">
          <div className="songs-head-title">
            Danh sách songs ({loading ? "..." : songsArr.length})
          </div>
        </div>

        <div className="list songs-list">
          {loading && <div className="note">Loading...</div>}

          {!loading && songsArr.length === 0 && <div className="note">Không có bài nào.</div>}

          {!loading &&
            songsArr.map((s) => (
              <div key={s.dbKey} className="row songs-row">
                <div className="songs-left">
                  <div className="songs-title">
                    {s.title || "(no title)"} <span className="songs-dot">•</span>{" "}
                    <span className="songs-dim">{s.artist || "(no artist)"}</span>{" "}
                    <span className="songs-dot">•</span>{" "}
                    <span className="songs-tag">{s.category || "no category"}</span>
                  </div>

                  <div className="songs-meta">
                    <div>
                      <b>dbKey:</b> {s.dbKey} <span className="songs-dot">•</span> <b>id:</b>{" "}
                      {String(s.id)}
                    </div>

                    <div className="songs-metaline">
                      <b>coverUrl:</b> <span className="songs-mono">{s.coverUrl || "-"}</span>
                    </div>

                    <div className="songs-metaline">
                      <b>mp3Url:</b> <span className="songs-mono">{s.mp3Url || "-"}</span>
                    </div>
                  </div>
                </div>

                <div className="songs-actions">
                  <button className="btn" onClick={() => openEditSong(s)}>
                    Edit
                  </button>
                  <button className="btn btn-danger" onClick={() => deleteSong(s.dbKey)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
        </div>
      </div>

      {/* ============ ADD MODAL ============ */}
      {openAdd && (
        <Modal title="➕ Thêm bài hát (ID tự sinh)" onClose={() => !adding && setOpenAdd(false)}>
          <div className="grid2">
            <Field
              label="Title"
              value={addForm.title}
              onChange={(v) => setAddForm((p) => ({ ...p, title: v }))}
              placeholder="Nhập title..."
            />
            <Field
              label="Artist"
              value={addForm.artist}
              onChange={(v) => setAddForm((p) => ({ ...p, artist: v }))}
              placeholder="Nhập artist..."
            />
          </div>

          <div style={{ marginTop: 10 }}>
            <div className="label">Category</div>
            <select
              className="input"
              value={addForm.category}
              onChange={(e) => setAddForm((p) => ({ ...p, category: e.target.value }))}
            >
              <option value="">-- chọn category --</option>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div style={{ marginTop: 10 }}>
            <Field
              label="coverUrl (link ảnh)"
              value={addForm.coverUrl}
              onChange={(v) => setAddForm((p) => ({ ...p, coverUrl: v }))}
              placeholder="https://... (postimg, imgur, ...)"
            />
          </div>

          <div style={{ marginTop: 10 }}>
            <Field
              label="mp3Url (link mp3)"
              value={addForm.mp3Url}
              onChange={(v) => setAddForm((p) => ({ ...p, mp3Url: v }))}
              placeholder="https://... (drive direct link, ...)"
            />
          </div>

          <div className="note">
            * Bạn không dùng Storage nên chỉ dán link public. Realtime Database không lưu file trực tiếp.
          </div>

          <div className="songs-modal-actions">
            <button className="btn btn-primary" onClick={submitAdd} disabled={adding}>
              {adding ? "Saving..." : "Save"}
            </button>
            <button className="btn" onClick={() => setOpenAdd(false)} disabled={adding}>
              Cancel
            </button>
          </div>
        </Modal>
      )}

      {/* ============ EDIT MODAL ============ */}
      {openEdit && (
        <Modal
          title={`✏️ Edit Song (dbKey: ${editKey})`}
          onClose={() => !editing && setOpenEdit(false)}
        >
          <div className="grid2">
            <Field
              label="Title"
              value={editForm.title}
              onChange={(v) => setEditForm((p) => ({ ...p, title: v }))}
              placeholder="Nhập title..."
            />
            <Field
              label="Artist"
              value={editForm.artist}
              onChange={(v) => setEditForm((p) => ({ ...p, artist: v }))}
              placeholder="Nhập artist..."
            />
          </div>

          <div style={{ marginTop: 10 }}>
            <div className="label">Category</div>
            <select
              className="input"
              value={editForm.category}
              onChange={(e) => setEditForm((p) => ({ ...p, category: e.target.value }))}
            >
              <option value="">-- chọn category --</option>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div style={{ marginTop: 10 }}>
            <Field
              label="coverUrl"
              value={editForm.coverUrl}
              onChange={(v) => setEditForm((p) => ({ ...p, coverUrl: v }))}
              placeholder="https://..."
            />
          </div>

          <div style={{ marginTop: 10 }}>
            <Field
              label="mp3Url"
              value={editForm.mp3Url}
              onChange={(v) => setEditForm((p) => ({ ...p, mp3Url: v }))}
              placeholder="https://..."
            />
          </div>

          <div className="songs-modal-actions">
            <button className="btn btn-primary" onClick={submitEdit} disabled={editing}>
              {editing ? "Saving..." : "Save changes"}
            </button>
            <button className="btn" onClick={() => setOpenEdit(false)} disabled={editing}>
              Cancel
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}

/* ---------------- UI helpers ---------------- */

function Field({ label, value, onChange, placeholder }) {
  return (
    <div>
      <div className="label">{label}</div>
      <input
        className="input"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
      />
    </div>
  );
}

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
