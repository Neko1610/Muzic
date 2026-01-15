import { useCallback, useState } from "react";

export default function DropzoneUpload({
  label,
  accept,
  file,
  setFile,
  hint,
}) {
  const [drag, setDrag] = useState(false);

  const onDrop = useCallback(
    (e) => {
      e.preventDefault();
      setDrag(false);
      const f = e.dataTransfer.files?.[0];
      if (!f) return;
      if (accept && !f.type.match(accept)) {
        alert("File không đúng định dạng: " + f.type);
        return;
      }
      setFile(f);
    },
    [accept, setFile]
  );

  const onPick = (e) => {
    const f = e.target.files?.[0];
    if (!f) return;
    if (accept && !f.type.match(accept)) {
      alert("File không đúng định dạng: " + f.type);
      return;
    }
    setFile(f);
  };

  return (
    <div style={{ marginTop: 10 }}>
      <div style={{ fontWeight: 800, marginBottom: 6 }}>{label}</div>

      <label
        onDragEnter={(e) => {
          e.preventDefault();
          setDrag(true);
        }}
        onDragOver={(e) => {
          e.preventDefault();
          setDrag(true);
        }}
        onDragLeave={(e) => {
          e.preventDefault();
          setDrag(false);
        }}
        onDrop={onDrop}
        style={{
          display: "block",
          padding: 14,
          borderRadius: 12,
          border: `1px dashed ${drag ? "#a78bfa" : "#334155"}`,
          background: drag ? "#1f2937" : "#0f172a",
          cursor: "pointer",
        }}
      >
        <input type="file" accept={accept?.replace(".*", "/*")} hidden onChange={onPick} />
        <div style={{ opacity: 0.85 }}>
          {file ? (
            <>
              <b>{file.name}</b> <span style={{ opacity: 0.7 }}>({Math.round(file.size / 1024)} KB)</span>
            </>
          ) : (
            <>Kéo thả file vào đây hoặc click để chọn</>
          )}
        </div>
        {hint && <div style={{ opacity: 0.6, fontSize: 12, marginTop: 6 }}>{hint}</div>}
      </label>
    </div>
  );
}
