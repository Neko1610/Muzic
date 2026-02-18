import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { auth, db } from "../firebase";
import { onValue, ref } from "firebase/database";
import { signOut } from "firebase/auth";

/* ===== Utils ===== */
function getInitial(nameOrEmail) {
  const s = String(nameOrEmail || "").trim();
  return s ? s[0].toUpperCase() : "A";
}

/* ✅ Detect Android (Android Studio / real device đều OK) */
function isAndroidApp() {
  if (typeof navigator === "undefined") return false;
  return /Android/i.test(navigator.userAgent);
}

export default function AdminLayout() {
  const [me, setMe] = useState(null);
  const [open, setOpen] = useState(false);
  const user = auth.currentUser;
  const navigate = useNavigate();

  const isAndroid = isAndroidApp();

  /* ===== Load user info ===== */
  useEffect(() => {
    if (!user?.uid) return;
    const r = ref(db, `users/${user.uid}`);
    const unsub = onValue(r, (snap) => setMe(snap.val() || null));
    return () => unsub();
  }, [user?.uid]);

  /* ===== Auto close sidebar when route changed (Android only) ===== */
  useEffect(() => {
    if (isAndroid) setOpen(false);
  }, [location.pathname]);

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
      {/* ===== SIDEBAR ===== */}
      <aside className={`sidebar ${open ? "open" : ""}`}>
        {/* PROFILE */}
        <div className="card profile">
          <div className="avatar">{getInitial(name || email)}</div>

          <div className="meta">
            <p className="name">{name}</p>
            <div className="email">{email}</div>
            <div className="badge">Role: {role}</div>
            <div style={{ marginTop: 10, fontSize: 12, opacity: 0.7 }}>
              UID: {uid.slice(0, 10)}...{uid.slice(-6)}
            </div>
          </div>
        </div>

        {/* NAV */}
        <div className="card nav">
          <h4>Các tab chức năng</h4>

          <NavItem to="/admin/content" icon="🎵" text="Songs" close={() => setOpen(false)} />
          <NavItem to="/admin/categories" icon="🏷️" text="Categories" close={() => setOpen(false)} />
          <NavItem to="/admin/playlists" icon="📚" text="Playlists" close={() => setOpen(false)} />
          <NavItem to="/admin/users" icon="👤" text="Users" close={() => setOpen(false)} />
          <NavItem to="/admin/analytics" icon="📊" text="Analytics" close={() => setOpen(false)} />

        </div>

        {/* LOGOUT */}
        <div className="card" style={{ marginTop: "auto" }}>
          <button
            className="btn-danger"
            style={{ width: "100%", padding: 12, borderRadius: 999 }}
            onClick={handleLogout}
          >
            LOG OUT
          </button>
        </div>
      </aside>

      {/* ===== MAIN ===== */}
      <div className="main-wrap">
        {/* TOPBAR – CHỈ ANDROID */}
        <div className="topbar">
          {isAndroid && (
            <button className="menu-btn" onClick={() => setOpen(!open)}>
              ☰
            </button>
          )}
        
        </div>

        <main className="card main">
          <Outlet />
        </main>
      </div>

      {/* OVERLAY – Android only */}
      {isAndroid && open && (
        <div className="overlay" onClick={() => setOpen(false)} />
      )}
    </div>
  );
}

/* ===== NavItem ===== */
function NavItem({ to, icon, text, close }) {
  return (
    <NavLink
      to={to}
      onClick={close}
      className={({ isActive }) => (isActive ? "active" : "")}
    >
      <span className="icon">{icon}</span>
      {text}
    </NavLink>
  );
}
