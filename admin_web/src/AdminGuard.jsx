import { useEffect, useState } from "react";
import { onAuthStateChanged } from "firebase/auth";
import { ref, get } from "firebase/database";
import { auth, db } from "./firebase";
import { Navigate } from "react-router-dom";

export default function AdminGuard({ children }) {
  const [ok, setOk] = useState(null);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (user) => {
      if (!user) return setOk(false);

      const snap = await get(ref(db, `users/${user.uid}/role`));
      setOk(snap.val() === "admin");
    });

    return () => unsub();
  }, []);

  if (ok === null) return <div style={{ padding: 24 }}>Loading...</div>;
  if (!ok) return <Navigate to="/login" replace />;

  return children;
}
