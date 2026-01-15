import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { auth, db } from "../firebase";
import { onValue, ref } from "firebase/database";
import { signOut } from "firebase/auth";

function getInitial(nameOrEmail) {
  const s = String(nameOrEmail || "").trim();
  return s ? s[0].toUpperCase() : "A";
}

export default function AdminLayout() {
  const [me, setMe] = useState(null);
  const user = auth.currentUser;
  const navigate = useNavigate();

  useEffect(() => {
    if (!user?.uid) return;
    const r = ref(db, `users/${user.uid}`);
    const unsub = onValue(r, (snap) => setMe(snap.val() || null));
    return () => unsub();
  }, [user?.uid]);

  const name = me?.name || user?.displayName || "admin";
  const email = me?.email || user?.email || "(no email)";
  const role = me?.role || "user";
  const uid = user?.uid || "";

  const handleLogout = async () => {
    const ok = window.confirm("Bạn có chắc muốn đăng xuất?");
    if (!ok) return;
    await signOut(auth);
    navigate("/login");
  };

  return (
    <div className="admin-shell">
      <aside className="sidebar">
        {/* PROFILE */}
        <div className="card profile">
          <div className="avatar">{getInitial(name || email)}</div>

          <div className="meta">
            <p className="name">{name}</p>
            <div className="email">{email}</div>
            <div className="badge">Role: {role}</div>
            <div style={{ marginTop: 10, color: "var(--muted)", fontSize: 12 }}>
              UID: {uid.slice(0, 10)}...{uid.slice(-6)}
            </div>
          </div>
        </div>

        {/* NAV */}
        <div className="card nav">
          <h4>Các tab chức năng</h4>

          <NavItem to="/admin/content" icon="🎵" text="Songs" />
          <NavItem to="/admin/categories" icon="🏷️" text="Categories" />
          <NavItem to="/admin/playlists" icon="📚" text="Playlists" />
          <NavItem to="/admin/users" icon="👤" text="Users" />

          {/* ❌ ĐÃ ẨN ROLES */}
        </div>

        {/* LOGOUT – GHIM DƯỚI */}
        <div className="card" style={{ marginTop: "auto" }}>
          <button
            className="btn-danger"
            style={{ width: "100%", padding: "12px", fontWeight: 900 ,borderRadius: 999,}}
            onClick={handleLogout}
          >
            LOG OUT
          </button>
        </div>
      </aside>

      <main className="card main">
        <Outlet />
      </main>
    </div>
  );
}

function NavItem({ to, icon, text }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) => (isActive ? "active" : "")}
      end={false}
    >
      <span className="icon">{icon}</span>
      {text}
    </NavLink>
  );
}
