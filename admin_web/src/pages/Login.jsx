import { useMemo, useState } from "react";
import { signInWithEmailAndPassword } from "firebase/auth";
import { ref, get } from "firebase/database";
import { auth, db } from "../firebase";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [email, setEmail] = useState("");
  const [pass, setPass] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  // ✅ thay ảnh này thành ảnh bạn muốn (có thể để trống "")
  const ILLU_URL = "/login-girl.png"; // ví dụ: đặt ảnh trong public/login-girl.png

  const canLogin = useMemo(() => {
    return email.trim() && pass.trim() && !loading;
  }, [email, pass, loading]);

  const login = async () => {
    if (!email.trim()) return alert("Nhập email");
    if (!pass.trim()) return alert("Nhập password");

    setLoading(true);
    try {
      // 1) login auth
      const cred = await signInWithEmailAndPassword(auth, email.trim(), pass);
      const uid = cred.user.uid;

      // 2) check role trong DB
      const roleSnap = await get(ref(db, `users/${uid}/role`));
      const role = roleSnap.val();

      if (role === "admin") nav("/admin");
      else alert("Bạn không có quyền admin!");
    } catch (e) {
      alert("Login lỗi: " + e.message);
    } finally {
      setLoading(false);
    }
  };

  const onEnter = (e) => {
    if (e.key === "Enter") login();
  };

  return (
    <div style={styles.page}>
      <div style={styles.shell}>
        {/* LEFT: FORM */}
        <div style={styles.left}>
          <div style={styles.brandRow}>
            <div style={styles.logoDot} />
            <div style={styles.brandText}>ADMIN</div>
          </div>

          <div style={styles.title}>LOGIN</div>
          <div style={styles.sub}>
            Vui lòng đăng nhập bằng tài khoản admin.
          </div>

          <div style={{ marginTop: 18 }}>
            <div style={styles.label}>Username</div>
            <div style={styles.inputWrap}>
              <span style={styles.inputIcon}>👤</span>
              <input
                style={styles.input}
                placeholder="Email admin"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyDown={onEnter}
                autoComplete="email"
              />
            </div>

            <div style={{ ...styles.label, marginTop: 12 }}>Password</div>
            <div style={styles.inputWrap}>
              <span style={styles.inputIcon}>🔒</span>
              <input
                style={styles.input}
                placeholder="Password"
                type="password"
                value={pass}
                onChange={(e) => setPass(e.target.value)}
                onKeyDown={onEnter}
                autoComplete="current-password"
              />
            </div>

            <button
              style={{
                ...styles.loginBtn,
                opacity: canLogin ? 1 : 0.6,
                cursor: canLogin ? "pointer" : "not-allowed",
              }}
              onClick={login}
              disabled={!canLogin}
            >
              {loading ? "Logging in..." : "Login now"}
            </button>
          </div>
        </div>

        {/* RIGHT: ILLU */}
        <div style={styles.right}>
          <div style={styles.rightDecor1} />
          <div style={styles.rightDecor2} />

          <div style={styles.illuCard}>
            {ILLU_URL ? (
              <img
                src={ILLU_URL}
                alt="illustration"
                style={styles.illuImg}
              />
            ) : (
              <div style={styles.illuPlaceholder}>
                (Thêm ảnh vào public/login-girl.png)
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    display: "grid",
    placeItems: "center",
    padding: 24,
    background:
      "radial-gradient(1200px 600px at 15% 10%, rgba(124, 58, 237, 0.18), transparent 60%)," +
      "radial-gradient(900px 520px at 90% 30%, rgba(59, 130, 246, 0.16), transparent 55%)," +
      "#f5f7fb",
    fontFamily: "system-ui, -apple-system, Segoe UI, Roboto, Arial",
  },

  shell: {
    width: 980,
    maxWidth: "95vw",
    minHeight: 520,
    background: "#fff",
    borderRadius: 26,
    boxShadow: "0 18px 45px rgba(15, 23, 42, 0.12)",
    border: "1px solid rgba(226,232,240,0.9)",
    overflow: "hidden",
    display: "grid",
    gridTemplateColumns: "1.05fr 1fr",
  },

  left: {
    padding: 44,
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
  },

  brandRow: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    marginBottom: 18,
  },
  logoDot: {
    width: 16,
    height: 16,
    borderRadius: 999,
    background: "linear-gradient(135deg, #7c3aed, #3b82f6)",
  },
  brandText: {
    fontWeight: 900,
    letterSpacing: 0.4,
    color: "#111827",
    opacity: 0.85,
    fontSize: 13,
  },

  title: {
    fontSize: 28,
    fontWeight: 950,
    color: "#111827",
    letterSpacing: 0.2,
  },
  sub: {
    marginTop: 8,
    color: "#64748b",
    fontSize: 13,
    lineHeight: 1.4,
    maxWidth: 340,
  },

  label: {
    fontSize: 17,
    fontWeight: 900,
    color: "#0f172a",
    marginBottom: 8,
  },

  inputWrap: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    border: "1px solid #e5e7eb",
    borderRadius: 999,
    padding: "10px 14px",
    background: "#fff",
  },
  inputIcon: {
    opacity: 0.7,
    fontSize: 14,
  },
  input: {
    border: "none",
    outline: "none",
    width: "100%",
    fontSize: 20,
    color: "#0f172a",
  },

  loginBtn: {
    marginTop: 18,
    width: "100%",
    padding: "12px 14px",
    borderRadius: 999,
    border: "1px solid rgba(124,58,237,0.25)",
    background: "linear-gradient(135deg, #7c3aed, #3b82f6)",
    color: "#fff",
    fontWeight: 950,
  },

  right: {
    position: "relative",
    background:
      "linear-gradient(135deg, rgba(124,58,237,0.95), rgba(59,130,246,0.92))",
    display: "grid",
    placeItems: "center",
    padding: 26,
  },

  rightDecor1: {
    position: "absolute",
    width: 240,
    height: 240,
    borderRadius: 999,
    background: "rgba(255,255,255,0.12)",
    top: -60,
    left: -60,
    filter: "blur(0px)",
  },
  rightDecor2: {
    position: "absolute",
    width: 220,
    height: 220,
    borderRadius: 999,
    background: "rgba(255,255,255,0.10)",
    bottom: -70,
    right: -70,
  },

  illuCard: {
    width: "72%",
    maxWidth: 340,
    aspectRatio: "1 / 1",
    borderRadius: 22,
    background: "rgba(255,255,255,0.18)",
    border: "1px solid rgba(255,255,255,0.25)",
    boxShadow: "0 16px 35px rgba(2, 6, 23, 0.22)",
    display: "grid",
    placeItems: "center",
    overflow: "hidden",
    backdropFilter: "blur(8px)",
  },
  illuImg: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },
  illuPlaceholder: {
    color: "rgba(255,255,255,0.85)",
    fontWeight: 900,
    fontSize: 12,
    padding: 14,
    textAlign: "center",
  },
};
