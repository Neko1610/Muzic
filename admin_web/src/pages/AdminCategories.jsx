import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue, set, update, remove } from "firebase/database";

/* ===== helper ===== */
const safe = (v, fb = "") => (v == null ? fb : v);

export default function AdminCategories() {
  /* ======================
     CATEGORIES
  ====================== */
  const [catMap, setCatMap] = useState({});
  const [q, setQ] = useState("");
  const [selectedCat, setSelectedCat] = useState(null);

  useEffect(() => {
    const r = ref(db, "categories");
    return onValue(r, (snap) => setCatMap(snap.val() || {}));
  }, []);

  const categories = useMemo(() => {
    const arr = Object.entries(catMap).map(([id, v]) => ({
      id,
      name: safe(v?.name),
    }));

    arr.sort((a, b) => a.name.localeCompare(b.name));

    const k = q.trim().toLowerCase();
    if (!k) return arr;

    return arr.filter((c) =>
      `${c.id} ${c.name}`.toLowerCase().includes(k)
    );
  }, [catMap, q]);

  /* ======================
     SONGS
  ====================== */
  const [songsMap, setSongsMap] = useState({});

  useEffect(() => {
    const r = ref(db, "songs");
    return onValue(r, (snap) => setSongsMap(snap.val() || {}));
  }, []);

  /* ======================
     FILTER SONGS BY CATEGORY
  ====================== */
 const songsByCategory = useMemo(() => {
  if (!selectedCat) return [];

  return Object.entries(songsMap)
    .map(([key, s]) => ({
      songKey: key,
      ...s,
    }))
    .filter((s) => {
      if (!s.category) return false;

      // "Classic,Trending" -> ["classic", "trending"]
      const cateArr = String(s.category)
        .split(",")
        .map((c) => c.trim().toLowerCase());

      return cateArr.includes(
        String(selectedCat.name).trim().toLowerCase()
      );
    });
}, [songsMap, selectedCat]);


  /* ======================
     ADD / EDIT CATEGORY
  ====================== */
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState("add");
  const [editId, setEditId] = useState("");
  const [name, setName] = useState("");

  const openAdd = () => {
    setMode("add");
    setEditId("");
    setName("");
    setOpen(true);
  };

  const openEdit = (c) => {
    setMode("edit");
    setEditId(c.id);
    setName(c.name);
    setOpen(true);
  };

  const save = async () => {
    const n = name.trim();
    if (!n) return alert("Nhập tên category");

    if (mode === "add") {
      const id = `cat_${Date.now()}`;
      await set(ref(db, `categories/${id}`), { name: n });
    } else {
      await update(ref(db, `categories/${editId}`), { name: n });
    }
    setOpen(false);
  };

  const del = async (id) => {
    if (!window.confirm("Xóa category này?")) return;
    await remove(ref(db, `categories/${id}`));
    if (selectedCat?.id === id) setSelectedCat(null);
  };

  return (
    <div>
      {/* HEADER */}
      <div className="page-title">
        <h1>Categories</h1>
        <button className="btn btn-primary" onClick={openAdd}>
          + Add Category
        </button>
      </div>

      {/* GRID */}
      <div className="pl-grid">
        {/* LEFT */}
        <div className="card pl-card">
          <input
            className="input"
            placeholder="Search category..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />

          <div className="list" style={{ marginTop: 12 }}>
            {categories.map((c) => (
              <div
                key={c.id}
                className={`pl-item ${
                  selectedCat?.id === c.id ? "is-active" : ""
                }`}
              >
                <button
                  className="pl-item-btn"
                  onClick={() => setSelectedCat(c)}
                >
                  <div className="pl-item-name">{c.name}</div>
                  <div className="pl-item-sub">id: {c.id}</div>
                </button>

                <div className="row-actions">
                  <button className="btn" onClick={() => openEdit(c)}>
                    Edit
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => del(c.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}

            {categories.length === 0 && (
              <div className="note">Chưa có category</div>
            )}
          </div>
        </div>

        {/* RIGHT */}
        <div className="card pl-card">
          {!selectedCat ? (
            <div className="note">Chọn category để xem nhạc</div>
          ) : (
            <>
              <div className="pl-title">
                Category: {selectedCat.name}
              </div>

              <div className="pl-section">
                Songs ({songsByCategory.length})
              </div>

              <div className="pl-songs">
                {songsByCategory.length === 0 ? (
                  <div className="note">Không có bài</div>
                ) : (
                  songsByCategory.map((s) => (
                    <div key={s.songKey} className="pl-song-row">
                      <div>
                        <div className="pl-song-text">
                          {s.title || "(no title)"}{" "}
                          <span className="pl-song-artist">
                            • {s.artist || "(no artist)"}
                          </span>
                        </div>

                        {/* CATEGORY BADGES */}
                       <div style={{ marginTop: 4 }}>
  {String(s.category || "")
    .split(",")
    .map((c) => c.trim())
    .filter(Boolean)
    .map((c) => (
      <span
        key={c}
        style={{
          display: "inline-block",
          padding: "2px 8px",
          marginRight: 6,
          borderRadius: 999,
          fontSize: 12,
          background: "#e0e7ff",
          color: "#1e40af",
          fontWeight: 600,
        }}
      >
        {c}
      </span>
    ))}
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

      {/* MODAL */}
      {open && (
        <Modal
          title={mode === "add" ? "➕ Add Category" : "✏️ Edit Category"}
          onClose={() => setOpen(false)}
        >
          <div className="label">Name</div>
          <input
            className="input"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />

          <div style={{ display: "flex", gap: 10, marginTop: 14 }}>
            <button className="btn btn-primary" onClick={save}>
              Save
            </button>
            <button className="btn" onClick={() => setOpen(false)}>
              Cancel
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}

/* ===== MODAL ===== */
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
