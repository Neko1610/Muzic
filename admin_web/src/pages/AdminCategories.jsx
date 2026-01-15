import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue, set, update, remove } from "firebase/database";

export default function AdminCategories() {
  const [catMap, setCatMap] = useState({});
  const [q, setQ] = useState("");

  // add/edit
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState("add"); // add|edit
  const [editId, setEditId] = useState("");
  const [name, setName] = useState("");

  useEffect(() => {
    const r = ref(db, "categories");
    const unsub = onValue(r, (snap) => setCatMap(snap.val() || {}));
    return () => unsub();
  }, []);

  const cats = useMemo(() => {
    const arr = Object.entries(catMap).map(([id, v]) => {
      const n = typeof v === "string" ? v : (v?.name ?? "");
      return { id, name: n };
    });

    arr.sort((a, b) => (a.name || "").localeCompare(b.name || ""));

    const k = q.trim().toLowerCase();
    if (!k) return arr;

    return arr.filter((c) => (`${c.id} ${c.name}`.toLowerCase().includes(k)));
  }, [catMap, q]);

  const openAdd = () => {
    setMode("add");
    setEditId("");
    setName("");
    setOpen(true);
  };

  const openEdit = (c) => {
    setMode("edit");
    setEditId(c.id);
    setName(c.name || "");
    setOpen(true);
  };

  const save = async () => {
    const n = name.trim();
    if (!n) return alert("Nhập tên category");

    if (mode === "add") {
      // id tự sinh theo timestamp
      const id = `cat_${Date.now()}`;
      await set(ref(db, `categories/${id}`), { name: n });
      setOpen(false);
      return;
    }

    // edit
    await update(ref(db, `categories/${editId}`), { name: n });
    setOpen(false);
  };

  const del = async (id) => {
    const ok = window.confirm("Xóa category này?");
    if (!ok) return;
    await remove(ref(db, `categories/${id}`));
  };

  return (
    <div>
      {/* Header theo style chung */}
      <div className="page-title">
        <div>
          <h1>Categories</h1>
          
        </div>

        <button className="btn btn-primary" onClick={openAdd}>
          + Add Category
        </button>
      </div>

      {/* Card */}
      <div className="card" style={{ padding: 16 }}>
        <input
          className="input"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search category..."
        />

        <div style={{ marginTop: 12, color: "var(--muted)" }}>
          Tổng: <b style={{ color: "var(--text)" }}>{cats.length}</b>
        </div>

        {/* List */}
        <div className="list" style={{ maxHeight: "62vh" }}>
          {cats.map((c) => (
            <div key={c.id} className="row">
              <div className="row-head">
                <div style={{ minWidth: 0 }}>
                  <div className="row-title">{c.name || "(no name)"}</div>
                  <div className="row-sub">id: {c.id}</div>
                </div>

                <div className="row-actions">
                  <button className="btn" onClick={() => openEdit(c)}>
                    Edit
                  </button>
                  <button
                    className="btn"
                    onClick={() => del(c.id)}
                    style={{
                      borderColor: "rgba(239,68,68,0.35)",
                      background: "rgba(239,68,68,0.12)",
                      color: "#b91c1c",
                      fontWeight: 900,
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}

          {cats.length === 0 && (
            <div style={{ color: "var(--muted)", padding: 12 }}>
              Chưa có category.
            </div>
          )}
        </div>
      </div>

      {/* Modal */}
      {open && (
        <Modal
          title={mode === "add" ? "➕ Add Category" : `✏️ Edit Category (${editId})`}
          onClose={() => setOpen(false)}
        >
          <div className="label">Name</div>
          <input
            className="input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="VD: KPop"
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

/* ============== Modal dùng class chung ============== */
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
