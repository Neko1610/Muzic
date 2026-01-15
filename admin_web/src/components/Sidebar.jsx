import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { getAuth, signOut } from "firebase/auth";

export default function AdminSidebar({ user }) {
  const auth = getAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    const ok = window.confirm("Bạn có chắc muốn đăng xuất?");
    if (!ok) return;

    await signOut(auth);
    navigate("/login");
  };

  // hiển thị uid gọn
  const uidShort = user?.uid
    ? `${user.uid.slice(0, 10)}...${user.uid.slice(-6)}`
    : "";

  return (
    <aside className="sidebar">
      {/* PROFILE */}
      <div className="card profile">
        <div className="avatar">{user?.name?.[0]?.toUpperCase() || "A"}</div>

        <div className="meta">
          <p className="name">{user?.name || "admin"}</p>
          <p className="email">{user?.email || "(no email)"}</p>

          <span className="badge">Role: {user?.role || "user"}</span>

          <div style={{ marginTop: 10, fontSize: 12, color: "var(--muted)" }}>
            UID: {uidShort}
          </div>
        </div>
      </div>

      {/* NAV */}
      <div className="card nav">
        <h4>Các tab chức năng</h4>

        {/* ⚠️ route của mày đang là /admin/content (Songs) */}
        <NavLink
          to="/admin/content"
          className={({ isActive }) => (isActive ? "active" : "")}
        >
          <span className="icon">🎵</span> Songs
        </NavLink>

        <NavLink
          to="/admin/categories"
          className={({ isActive }) => (isActive ? "active" : "")}
        >
          <span className="icon">🏷️</span> Categories
        </NavLink>

        <NavLink
          to="/admin/playlists"
          className={({ isActive }) => (isActive ? "active" : "")}
        >
          <span className="icon">📚</span> Playlists
        </NavLink>

        <NavLink
          to="/admin/users"
          className={({ isActive }) => (isActive ? "active" : "")}
        >
          <span className="icon">👤</span> Users
        </NavLink>

        {/* ❌ Ẩn Roles: không render NavLink roles */}
      </div>

      {/* LOGOUT – GHIM DƯỚI CÙNG */}
      <div className="card" style={{ marginTop: "auto" }}>
        <button
          className="btn-danger"
          style={{ width: "100%", padding: "12px", fontWeight: 900 }}
          onClick={handleLogout}
        >
          🚪 Đăng xuất
        </button>
      </div>
    </aside>
  );
}
