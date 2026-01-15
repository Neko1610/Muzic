import { useEffect, useState } from "react";
import { ref, get } from "firebase/database";
import { db } from "./firebase";

export default function TestFirebase() {
  const [data, setData] = useState(null);

  useEffect(() => {
    get(ref(db, "users"))
      .then((snap) => setData(snap.val()))
      .catch((e) => console.error("Firebase read error:", e));
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <h2>Test Firebase</h2>
      <pre style={{ background: "#111", color: "#0f0", padding: 12 }}>
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}
